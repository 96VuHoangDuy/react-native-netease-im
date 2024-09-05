//
//  ChatroomViewController.h
//  RNNeteaseIm
//
//  Created by Rêu on 5/9/24.
//  Copyright © 2024 Kinooo. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "NIMModel.h"

typedef void(^Success)(id _Nullable params);
typedef void(^Errors)(id _Nullable error);
@interface ChatroomViewController : UIViewController<NIMChatroomManagerDelegate>
+(instancetype)initWithChatroomViewController;

@end
