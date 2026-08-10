//
//  RNNIMCsCallControllers.m
//  RNNeteaseIm
//

#import "RNNIMCsCallControllers.h"
#import "RNNIMCsCallBranding.h"
#import <CoreImage/CoreImage.h>

// Tag guard cho container nền + scrim — refreshUI của SDK gọi lại nhiều lần, chỉ add 1 lần.
static const NSInteger kRNNIMCsBgContainerTag = 0x63736267; // 'csbg'
static const NSInteger kRNNIMCsBgScrimTag = 0x63737363;     // 'cssc'

/**
 * Blur ảnh MỘT LẦN bằng CoreImage rồi hiển thị bản đã blur — KHÔNG dùng UIVisualEffectView:
 * máy bật Reduce Transparency (Trợ năng) render effect view thành màu đặc gần đen che kín ảnh
 * (đúng ca máy thật đen xì trong khi sim hiện blur). Pre-blur cho kết quả giống nhau mọi máy.
 * Downscale trước cho rẻ (nền blur không cần độ phân giải); blur fail hiếm → trả bản downscale.
 */
static UIImage *RNNIMBlurredImage(UIImage *src) {
    if (src == nil) return nil;
    CGFloat maxDim = MAX(src.size.width, src.size.height);
    CGFloat scale = maxDim > 240.0 ? 240.0 / maxDim : 1.0;
    CGSize smallSize = CGSizeMake(MAX(src.size.width * scale, 1), MAX(src.size.height * scale, 1));
    UIGraphicsBeginImageContextWithOptions(smallSize, YES, 1);
    [src drawInRect:CGRectMake(0, 0, smallSize.width, smallSize.height)];
    UIImage *small = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    if (small.CGImage == NULL) return src;
    static CIContext *ciContext;
    static dispatch_once_t once;
    dispatch_once(&once, ^{ ciContext = [CIContext context]; });
    CIImage *input = [CIImage imageWithCGImage:small.CGImage];
    // clampToExtent trước khi blur để mép ảnh không bị viền trong suốt, rồi crop về extent gốc.
    CIImage *blurred = [[input imageByClampingToExtent]
        imageByApplyingGaussianBlurWithSigma:12.0];
    CGImageRef out = [ciContext createCGImage:blurred fromRect:input.extent];
    if (out == NULL) return small;
    UIImage *result = [UIImage imageWithCGImage:out];
    CGImageRelease(out);
    return result;
}

/**
 * Container nền RIÊNG chèn đáy vc.view (index 0) — KHÔNG đụng view của SDK. Bài học: add blur vào
 * remoteBigAvatorView che luôn avatar ô nhỏ (ô nhỏ nằm trong cây view đó).
 */
static UIImageView *RNNIMBlurContainer(NECallUIStateController *vc) {
    if (vc.viewIfLoaded == nil) return nil;
    UIImageView *bg = (UIImageView *)[vc.view viewWithTag:kRNNIMCsBgContainerTag];
    if (bg != nil) return bg;
    bg = [[UIImageView alloc] initWithFrame:vc.view.bounds];
    bg.tag = kRNNIMCsBgContainerTag;
    bg.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    bg.contentMode = UIViewContentModeScaleAspectFill;
    bg.clipsToBounds = YES;
    bg.userInteractionEnabled = NO;
    // Scrim đen 0.3 giữ lại cho chữ trắng dễ đọc trên ảnh sáng.
    UIView *scrim = [[UIView alloc] initWithFrame:bg.bounds];
    scrim.tag = kRNNIMCsBgScrimTag;
    scrim.backgroundColor = [UIColor colorWithWhite:0 alpha:0.3];
    scrim.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    scrim.userInteractionEnabled = NO;
    [bg addSubview:scrim];
    [vc.view insertSubview:bg atIndex:0];
    NSLog(@"[RNNIMCsBG] container created reduceTransparency=%d",
          UIAccessibilityIsReduceTransparencyEnabled());
    return bg;
}

// ==== [RNNIMCsBG] LOG TẠM trace nền đen trên máy thật — XÓA toàn bộ sau khi fix xong ====
// Dump các subview top-level của vc.view: bắt trường hợp view SDK opaque nằm ĐÈ lên container index 0.
static void RNNIMLogBgViewStack(NECallUIStateController *vc, NSString *where) {
    if (vc.viewIfLoaded == nil) {
        NSLog(@"[RNNIMCsBG] stack(%@): view not loaded", where);
        return;
    }
    NSArray<UIView *> *subs = vc.view.subviews;
    NSLog(@"[RNNIMCsBG] stack(%@) vc=%@ count=%lu", where, NSStringFromClass(vc.class),
          (unsigned long)subs.count);
    for (NSUInteger i = 0; i < subs.count; i++) {
        UIView *v = subs[i];
        CGFloat white = 0, bgAlpha = 0;
        BOOL opaqueBg = [v.backgroundColor getWhite:&white alpha:&bgAlpha] && bgAlpha >= 0.99;
        BOOL hasImage = [v isKindOfClass:UIImageView.class] && ((UIImageView *)v).image != nil;
        NSLog(@"[RNNIMCsBG]   [%lu] %@ tag=0x%lx hidden=%d alpha=%.2f opaqueBg=%d hasImage=%d frame=%@",
              (unsigned long)i, NSStringFromClass(v.class), (long)v.tag, v.isHidden, v.alpha,
              opaqueBg, hasImage, NSStringFromCGRect(v.frame));
    }
}

// Scrim đậm/nhạt khác nhau theo nguồn ảnh: nền CSKH dựng trên nền TRẮNG nên cần tối hơn ảnh
// avatar thật mới ra xám trung (khớp Android). Container dựng 1 lần nên phải set lại mỗi lần áp.
static void RNNIMSetScrimAlpha(NECallUIStateController *vc, CGFloat alpha) {
    UIImageView *bg = RNNIMBlurContainer(vc);
    UIView *scrim = [bg viewWithTag:kRNNIMCsBgScrimTag];
    scrim.backgroundColor = [UIColor colorWithWhite:0 alpha:alpha];
}

/**
 * Nền CSKH trước khi blur: logo phóng center-crop kín khung 1:2 trên nền TRẮNG đặc.
 * KHÔNG đưa thẳng logo vào RNNIMBlurredImage: PNG nền trong suốt vẽ vào context opaque (nền đen)
 * cho ra mảng xanh tương phản cao, không phải nền xám nhạt như thiết kế.
 */
static UIImage *RNNIMCsBrandBackgroundBase(void) {
    UIImage *logo = [RNNIMCsCallBranding logoImage];
    if (logo == nil) return nil;
    CGSize size = CGSizeMake(120, 240);
    UIGraphicsBeginImageContextWithOptions(size, YES, 1);
    [UIColor.whiteColor setFill];
    UIRectFill(CGRectMake(0, 0, size.width, size.height));
    CGFloat scale = MAX(size.width / logo.size.width, size.height / logo.size.height);
    CGSize drawSize = CGSizeMake(logo.size.width * scale, logo.size.height * scale);
    [logo drawInRect:CGRectMake((size.width - drawSize.width) / 2,
                                (size.height - drawSize.height) / 2,
                                drawSize.width, drawSize.height)];
    UIImage *base = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return base;
}

static void RNNIMSetBlurBackgroundImage(NECallUIStateController *vc, UIImage *image) {
    if (image == nil) return;
    UIImageView *bg = RNNIMBlurContainer(vc);
    NSLog(@"[RNNIMCsBG] set-image containerNil=%d changed=%d", bg == nil,
          bg != nil && bg.image != image);
    if (bg != nil && bg.image != image) bg.image = image;
}

/**
 * Nền blur cho MỌI cuộc gọi (kiểu WeChat — máy thật SDK để nền đen trơn):
 * CSR → logo CSKH (local, sync); user thường → avatar theo URL callParam.remoteAvatar
 * (RNNIMFillCallUserInfo đã fill ở cả 2 chiều) — tự tải + cache, KHÔNG phụ thuộc timing load
 * ảnh của view SDK (set image không trigger layout nên chờ view SDK là ăn race trên máy chậm).
 */
// Cache ảnh nền + danh sách VC đang chờ ảnh về, dùng chung cho apply-bg và prefetch.
// Main-thread only. Key tồn tại trong sBgWaiters = download url đó đang inflight.
static NSCache<NSString *, UIImage *> *sBgCache;
static NSMutableDictionary<NSString *, NSHashTable<NECallUIStateController *> *> *sBgWaiters;

static void RNNIMBgEnsureStores(void) {
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        sBgCache = [[NSCache alloc] init];
        sBgWaiters = [NSMutableDictionary dictionary];
    });
}

/**
 * Tải ảnh nền cho url (nếu chưa có download chạy) và fan-out cho MỌI VC đang chờ khi ảnh về.
 * Bài học từ máy thật: dedupe kiểu inflight-set nuốt mất requester thứ 2 (in-call VC xin ảnh
 * lúc download của màn callee đang chạy → không bao giờ nhận ảnh → đen vĩnh viễn), còn completion
 * chỉ set cho 1 weakVc thì VC đó thường đã off-screen lúc ảnh về. vcOrNil = nil khi prefetch.
 */
static void RNNIMBgRequestDownload(NSString *urlString, NECallUIStateController *vcOrNil) {
    RNNIMBgEnsureStores();
    NSHashTable<NECallUIStateController *> *table = sBgWaiters[urlString];
    if (table != nil) {
        if (vcOrNil != nil) [table addObject:vcOrNil];
        NSLog(@"[RNNIMCsBG] waiting (inflight, addedWaiter=%d)", vcOrNil != nil);
        return;
    }
    NSURL *url = [NSURL URLWithString:urlString];
    if (url == nil) {
        NSLog(@"[RNNIMCsBG] skip: bad url");
        return;
    }
    table = [NSHashTable weakObjectsHashTable];
    if (vcOrNil != nil) [table addObject:vcOrNil];
    sBgWaiters[urlString] = table;
    NSLog(@"[RNNIMCsBG] download start");
    [[[NSURLSession sharedSession] dataTaskWithURL:url
        completionHandler:^(NSData *data, NSURLResponse *resp, NSError *error) {
            UIImage *decoded = data.length ? [UIImage imageWithData:data] : nil;
            // Blur ngay trên thread background của NSURLSession — không chặn main.
            UIImage *image = RNNIMBlurredImage(decoded);
            NSInteger httpStatus = [resp isKindOfClass:NSHTTPURLResponse.class]
                ? ((NSHTTPURLResponse *)resp).statusCode : -1;
            NSLog(@"[RNNIMCsBG] download done err=%@/%ld http=%ld bytes=%lu decoded=%d blurred=%d",
                  error.domain, (long)error.code, (long)httpStatus, (unsigned long)data.length,
                  decoded != nil, image != nil);
            dispatch_async(dispatch_get_main_queue(), ^{
                NSHashTable<NECallUIStateController *> *doneTable = sBgWaiters[urlString];
                [sBgWaiters removeObjectForKey:urlString];
                if (image == nil) return; // lỗi tải → giữ nền mặc định, không retry
                [sBgCache setObject:image forKey:urlString];
                for (NECallUIStateController *waitingVc in doneTable) {
                    RNNIMSetBlurBackgroundImage(waitingVc, image);
                    RNNIMLogBgViewStack(waitingVc, @"after-download");
                }
            });
        }] resume];
}

void RNNIMCsPrefetchCallBackgroundAvatar(NSString *urlString) {
    if (urlString.length == 0) return;
    if (!NSThread.isMainThread) {
        // Waiters main-thread only; fill chạy trong callback SDK không đảm bảo main.
        dispatch_async(dispatch_get_main_queue(), ^{ RNNIMCsPrefetchCallBackgroundAvatar(urlString); });
        return;
    }
    RNNIMBgEnsureStores();
    if ([sBgCache objectForKey:urlString] != nil) return;
    NSLog(@"[RNNIMCsBG] prefetch urlLength=%lu", (unsigned long)urlString.length);
    RNNIMBgRequestDownload(urlString, nil);
}

static void RNNIMApplyCallBackground(NECallUIStateController *vc) {
    // [RNNIMCsBG] build marker: không thấy dòng này trong console = máy đang chạy bản CŨ.
    static dispatch_once_t markerOnce;
    dispatch_once(&markerOnce, ^{ NSLog(@"[RNNIMCsBG] build-marker blur-v4 loaded"); });
    BOOL isCsr = [RNNIMCsCallBranding isCsrAccid:vc.callParam.remoteUserAccid];
    // Chỉ log LENGTH của URL avatar, không log nội dung URL (thông tin user).
    NSLog(@"[RNNIMCsBG] apply-bg vc=%@ isCSR=%d avatarUrlLength=%lu", NSStringFromClass(vc.class),
          isCsr, (unsigned long)vc.callParam.remoteAvatar.length);
    if (isCsr) {
        // Logo cố định → blur 1 lần cache static (ảnh nhỏ, chi phí không đáng kể trên main).
        static UIImage *blurredLogo;
        static dispatch_once_t logoOnce;
        dispatch_once(&logoOnce, ^{ blurredLogo = RNNIMBlurredImage(RNNIMCsBrandBackgroundBase()); });
        RNNIMSetBlurBackgroundImage(vc, blurredLogo);
        RNNIMSetScrimAlpha(vc, 0.45);
        return;
    }
    NSString *urlString = vc.callParam.remoteAvatar;
    if (urlString.length == 0) {
        NSLog(@"[RNNIMCsBG] skip: empty avatar url");
        return; // không avatar → giữ nền đen mặc định
    }
    RNNIMBgEnsureStores();
    UIImage *cached = [sBgCache objectForKey:urlString];
    if (cached != nil) {
        NSLog(@"[RNNIMCsBG] cache hit");
        RNNIMSetBlurBackgroundImage(vc, cached);
        return;
    }
    RNNIMBgRequestDownload(urlString, vc);
}

// Ép tên + logo CSKH lên các view đã dựng sẵn của SDK. No-op với accid không phải CSR.
static void RNNIMApplyCsBranding(NECallUIStateController *vc) {
    if (![RNNIMCsCallBranding isCsrAccid:vc.callParam.remoteUserAccid]) return;
    NSString *name = [RNNIMCsCallBranding displayName];
    vc.centerTitleLabel.text = name;
    vc.titleLabel.text = name;
    // Ô avatar dùng bản NỀN TRẮNG ĐẶC: logo gốc nền trong suốt nên chìm vào nền blur tối.
    // backgroundColor trắng phòng trường hợp view rộng hơn ảnh; nền blur vẫn dùng logoImage.
    UIImage *avatar = [RNNIMCsCallBranding avatarImage];
    if (avatar) {
        vc.remoteAvatorView.image = avatar;
        vc.remoteAvatorView.backgroundColor = UIColor.whiteColor;
        vc.remoteAvatorView.contentMode = UIViewContentModeScaleAspectFill;
    }
}

// Text màn đổ chuông callee: SDK bundle chỉ có en/zh-Hans → máy tiếng Việt rơi về English.
// Đè bằng text localize từ JS (fallback tiếng Trung). Áp cho MỌI cuộc gọi, không riêng CSR.
static void RNNIMApplyCalleeLocalizedTexts(NECallUIStateController *vc) {
    vc.centerSubtitleLabel.text = [RNNIMCsCallBranding callControlLabelForKey:@"incomingSubtitle"];
    vc.acceptBtn.titleLabel.text = [RNNIMCsCallBranding callControlLabelForKey:@"accept"];
    vc.rejectBtn.titleLabel.text = [RNNIMCsCallBranding callControlLabelForKey:@"reject"];
}

@implementation RNNIMCsCalledViewController

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    RNNIMApplyCsBranding(self);
    RNNIMApplyCallBackground(self);
    RNNIMApplyCalleeLocalizedTexts(self);
}

- (void)refreshUI {
    [super refreshUI];
    RNNIMApplyCsBranding(self);
    RNNIMApplyCallBackground(self);
    RNNIMApplyCalleeLocalizedTexts(self);
    RNNIMLogBgViewStack(self, @"callee-refreshUI");
}

@end

@implementation RNNIMCsAudioCallingController

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    RNNIMApplyCsBranding(self);
    RNNIMApplyCallBackground(self);
}

- (void)refreshUI {
    [super refreshUI];
    RNNIMApplyCsBranding(self);
    RNNIMApplyCallBackground(self);
    RNNIMLogBgViewStack(self, @"calling-refreshUI");
}

@end

#pragma mark - Màn in-call: 3 nút to thay thanh pill nhỏ

// Khoảng cách giữa 2 nút liền kề và khoảng hở tới đáy safe area. Chọn để cụm nút nằm cùng vùng
// với cụm nút của màn caller/callee.
static const CGFloat kRNNIMCsInCallButtonSpacing = 44.0;
static const CGFloat kRNNIMCsInCallBottomInset = 44.0;
static const CGFloat kRNNIMCsInCallLabelFontSize = 14.0;

static NSBundle *RNNIMCallUIKitBundle(void) {
    return [NSBundle bundleForClass:NECustomButton.class];
}

static UIImage *RNNIMCallUIKitImage(NSString *name) {
    return [UIImage imageNamed:name inBundle:RNNIMCallUIKitBundle() compatibleWithTraitCollection:nil];
}

static NSString *RNNIMCallUIKitText(NSString *key) {
    return NSLocalizedStringFromTableInBundle(key, nil, RNNIMCallUIKitBundle(), nil);
}

@interface RNNIMCsAudioInCallController ()
/// 3 nút to tự dựng. Nil = chưa dựng.
@property(nonatomic, strong, nullable) NECustomButton *csMicrophoneButton;
@property(nonatomic, strong, nullable) NECustomButton *csHangupButton;
@property(nonatomic, strong, nullable) NECustomButton *csSpeakerButton;
@end

@implementation RNNIMCsAudioInCallController

- (void)setupAudioInCallUI {
    [super setupAudioInCallUI];
    [self setupCsBigOperationButtons];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    RNNIMApplyCsBranding(self);
    RNNIMApplyCallBackground(self);
    [self setupCsBigOperationButtons];
}

- (void)refreshUI {
    [super refreshUI];
    RNNIMApplyCsBranding(self);
    RNNIMApplyCallBackground(self);
    [self syncCsOperationIcons];
    RNNIMLogBgViewStack(self, @"incall-refreshUI");
}

// Path accept-từ-màn-OS: SDK re-show pill (hidden=NO) sau khi mình ẩn one-shot → chốt chặn ở layout
// (mọi lần re-show đều kéo layout pass). alpha=0 là lưới thứ hai: SDK chỉ toggle hidden, không đụng alpha.
- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    if (self.csHangupButton != nil) {
        [self hideDefaultOperationBar];
    }
}

- (void)hideDefaultOperationBar {
    self.operationView.hidden = YES;
    self.operationView.alpha = 0;
    self.operationView.userInteractionEnabled = NO;
}

/**
 * Ẩn thanh pill mặc định (`operationView`) và dựng 3 nút to `NECustomButton` — cùng class mà màn
 * caller/callee dùng, nên có sẵn imageView + titleLabel + vùng chạm phủ cả label.
 *
 * Pill KHÔNG bị gỡ khỏi view tree: nó vẫn giữ toàn bộ logic mute mic / bật loa / cúp máy của SDK
 * cho trạng thái in-call. Nút của ta chỉ forward sự kiện chạm sang button gốc.
 */
- (void)setupCsBigOperationButtons {
    if (self.csHangupButton != nil || self.operationView == nil) {
        return;
    }
    [self hideDefaultOperationBar];

    // Title khởi tạo chỉ là placeholder — syncCsOperationIcons đè text theo trạng thái ngay sau đó.
    self.csMicrophoneButton = [self csButtonWithTitle:RNNIMCallUIKitText(@"call_micro_phone")
                                               action:@selector(csMicrophoneTapped)];
    self.csHangupButton = [self csButtonWithTitle:RNNIMCallUIKitText(@"call_cancel")
                                           action:@selector(csHangupTapped)];
    self.csSpeakerButton = [self csButtonWithTitle:RNNIMCallUIKitText(@"call_speaker")
                                            action:@selector(csSpeakerTapped)];

    UILayoutGuide *safeArea = self.view.safeAreaLayoutGuide;
    [NSLayoutConstraint activateConstraints:@[
        [self.csHangupButton.centerXAnchor constraintEqualToAnchor:self.view.centerXAnchor],
        [self.csHangupButton.bottomAnchor constraintEqualToAnchor:safeArea.bottomAnchor
                                                         constant:-kRNNIMCsInCallBottomInset],
        [self.csMicrophoneButton.topAnchor constraintEqualToAnchor:self.csHangupButton.topAnchor],
        [self.csMicrophoneButton.rightAnchor constraintEqualToAnchor:self.csHangupButton.leftAnchor
                                                            constant:-kRNNIMCsInCallButtonSpacing],
        [self.csSpeakerButton.topAnchor constraintEqualToAnchor:self.csHangupButton.topAnchor],
        [self.csSpeakerButton.leftAnchor constraintEqualToAnchor:self.csHangupButton.rightAnchor
                                                        constant:kRNNIMCsInCallButtonSpacing],
    ]];

    [self syncCsOperationIcons];
}

- (NECustomButton *)csButtonWithTitle:(NSString *)title action:(SEL)action {
    // NECustomButton lấy kích thước icon từ frame khởi tạo (imageView bị ghim width/height =
    // hằng số lấy từ frame này), chiều cao tổng do label quyết định.
    CGSize size = self.buttonSize;
    NECustomButton *button =
        [[NECustomButton alloc] initWithFrame:CGRectMake(0, 0, size.width, size.height)];
    button.translatesAutoresizingMaskIntoConstraints = NO;
    button.titleLabel.text = title;
    button.titleLabel.textAlignment = NSTextAlignmentCenter;
    button.titleLabel.textColor = UIColor.whiteColor;
    button.titleLabel.font = [UIFont systemFontOfSize:kRNNIMCsInCallLabelFontSize];
    [button.maskBtn addTarget:self action:action forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:button];
    return button;
}

- (void)csMicrophoneTapped {
    [self.operationView.microPhone sendActionsForControlEvents:UIControlEventTouchUpInside];
    [self syncCsOperationIcons];
}

- (void)csSpeakerTapped {
    [self.operationView.speakerBtn sendActionsForControlEvents:UIControlEventTouchUpInside];
    [self syncCsOperationIcons];
}

- (void)csHangupTapped {
    [self.operationView.hangupBtn sendActionsForControlEvents:UIControlEventTouchUpInside];
}

/// Trạng thái lấy từ chính button gốc của SDK (`selected` = mic đang mute / loa ngoài đang bật),
/// nên không lệch kể cả khi user đã bật/tắt từ màn caller trước lúc kết nối.
- (void)syncCsOperationIcons {
    if (self.csHangupButton == nil) {
        return;
    }
    [self hideDefaultOperationBar];
    BOOL microphoneMuted = self.operationView.microPhone.selected;
    BOOL speakerOn = self.operationView.speakerBtn.selected;
    // Dùng họ asset nút to (150x150, đã bake sẵn nền tròn trắng) như màn caller/callee, không dùng
    // họ `call_voice_*` / `call_speaker_*` (48x48 glyph trần dành cho thanh pill nhỏ tự vẽ nền).
    self.csMicrophoneButton.imageView.image =
        RNNIMCallUIKitImage(microphoneMuted ? @"micro_phone_mute" : @"micro_phone");
    self.csSpeakerButton.imageView.image =
        RNNIMCallUIKitImage(speakerOn ? @"speaker_on" : @"speaker_off");
    self.csHangupButton.imageView.image = RNNIMCallUIKitImage(@"call_cancel");
    // Text trạng thái kiểu WeChat (JS set localized cn/vi; fallback tiếng Trung).
    self.csMicrophoneButton.titleLabel.text =
        [RNNIMCsCallBranding callControlLabelForKey:microphoneMuted ? @"micOff" : @"micOn"];
    self.csSpeakerButton.titleLabel.text =
        [RNNIMCsCallBranding callControlLabelForKey:speakerOn ? @"speakerOn" : @"speakerOff"];
    self.csHangupButton.titleLabel.text = [RNNIMCsCallBranding callControlLabelForKey:@"hangup"];
}

@end
