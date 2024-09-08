//
//  ChatroomViewController.m
//  RNNeteaseIm
//
//  Created by Rêu on 5/9/24.
//  Copyright © 2024 Kinooo. All rights reserved.
//

#import "ChatroomViewController.h"
#import "ImConfig.h"

@interface ChatroomViewController ()<NIMChatroomManagerDelegate> {

}

@end

@implementation ChatroomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
}

+ (instancetype)initWithChatroomViewController {
    static ChatroomViewController *cvc = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        cvc = [[ChatroomViewController alloc]init];
        
    });
    return cvc;
}

- (instancetype)init {
    return self;
}

@end
