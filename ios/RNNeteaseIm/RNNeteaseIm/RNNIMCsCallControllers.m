//
//  RNNIMCsCallControllers.m
//  RNNeteaseIm
//

#import "RNNIMCsCallControllers.h"
#import "RNNIMCsCallBranding.h"

// Tag guard cho lớp blur + scrim trên nền — refreshUI của SDK gọi lại branding nhiều lần,
// chỉ được add đúng một lần.
static const NSInteger kRNNIMCsBgBlurTag = 0x6373626C;  // 'csbl'
static const NSInteger kRNNIMCsBgScrimTag = 0x63737363; // 'cssc'

/**
 * Nền = ảnh blur tối + scrim (đồng bộ Android CsCallUiUtils.applyBrandBlurBackground, kiểu WeChat).
 * Trên máy thật SDK để remoteBigAvatorView TRỐNG (nền đen trơn) → tự áp cho mọi cuộc gọi.
 * Avatar ô nhỏ (remoteAvatorView) giữ ảnh sắc nét, không đụng.
 */
static void RNNIMApplyBlurBackground(NECallUIStateController *vc, UIImage *image) {
    UIImageView *bg = vc.remoteBigAvatorView;
    if (bg == nil || image == nil) return;
    // Gọi lặp từ viewDidLayoutSubviews → chỉ set khi đổi, tránh decode lại mỗi layout pass.
    if (bg.image != image) bg.image = image;
    bg.contentMode = UIViewContentModeScaleAspectFill;
    bg.clipsToBounds = YES;
    if ([bg viewWithTag:kRNNIMCsBgBlurTag] == nil) {
        UIVisualEffectView *blur = [[UIVisualEffectView alloc]
            initWithEffect:[UIBlurEffect effectWithStyle:UIBlurEffectStyleDark]];
        blur.tag = kRNNIMCsBgBlurTag;
        blur.frame = bg.bounds;
        blur.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        blur.userInteractionEnabled = NO;
        [bg addSubview:blur];
    }
    if ([bg viewWithTag:kRNNIMCsBgScrimTag] == nil) {
        UIView *scrim = [[UIView alloc] initWithFrame:bg.bounds];
        scrim.tag = kRNNIMCsBgScrimTag;
        scrim.backgroundColor = [UIColor colorWithWhite:0 alpha:0.3];
        scrim.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        scrim.userInteractionEnabled = NO;
        [bg addSubview:scrim];
    }
}

/**
 * Nền blur cho MỌI cuộc gọi: CSR → logo CSKH; user thường → avatar người gọi (SDK load async vào
 * remoteAvatorView qua SDWebImage — lần đầu có thể nil, nên phải gọi lặp ở refreshUI +
 * viewDidLayoutSubviews đến khi bắt được ảnh).
 */
static void RNNIMApplyCallBackground(NECallUIStateController *vc) {
    UIImage *image = [RNNIMCsCallBranding isCsrAccid:vc.callParam.remoteUserAccid]
        ? [RNNIMCsCallBranding logoImage]
        : vc.remoteAvatorView.image;
    RNNIMApplyBlurBackground(vc, image);
}

// Ép tên + logo CSKH lên các view đã dựng sẵn của SDK. No-op với accid không phải CSR.
static void RNNIMApplyCsBranding(NECallUIStateController *vc) {
    if (![RNNIMCsCallBranding isCsrAccid:vc.callParam.remoteUserAccid]) return;
    NSString *name = [RNNIMCsCallBranding displayName];
    vc.centerTitleLabel.text = name;
    vc.titleLabel.text = name;
    UIImage *logo = [RNNIMCsCallBranding logoImage];
    if (logo) {
        vc.remoteAvatorView.image = logo;
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
}

// Avatar user thường load async — chờ ảnh về rồi mới áp nền blur được.
- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    RNNIMApplyCallBackground(self);
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
}

// Path accept-từ-màn-OS: SDK re-show pill (hidden=NO) sau khi mình ẩn one-shot → chốt chặn ở layout
// (mọi lần re-show đều kéo layout pass). alpha=0 là lưới thứ hai: SDK chỉ toggle hidden, không đụng alpha.
// Nền blur cũng re-apply ở đây: avatar user thường load async, chờ ảnh về mới áp được.
- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    RNNIMApplyCallBackground(self);
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
    self.csMicrophoneButton.imageView.image =
        RNNIMCallUIKitImage(microphoneMuted ? @"call_voice_off" : @"call_voice_on");
    self.csSpeakerButton.imageView.image =
        RNNIMCallUIKitImage(speakerOn ? @"call_speaker_on" : @"call_speaker_off");
    self.csHangupButton.imageView.image = RNNIMCallUIKitImage(@"call_cancel");
    // Text trạng thái kiểu WeChat (JS set localized cn/vi; fallback tiếng Trung).
    self.csMicrophoneButton.titleLabel.text =
        [RNNIMCsCallBranding callControlLabelForKey:microphoneMuted ? @"micOff" : @"micOn"];
    self.csSpeakerButton.titleLabel.text =
        [RNNIMCsCallBranding callControlLabelForKey:speakerOn ? @"speakerOn" : @"speakerOff"];
    self.csHangupButton.titleLabel.text = [RNNIMCsCallBranding callControlLabelForKey:@"hangup"];
}

@end
