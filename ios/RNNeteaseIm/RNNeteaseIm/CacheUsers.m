//
//  CacheUsers.m
//  RNNeteaseIm
//
//  Created by Rêu on 25/9/24.
//  Copyright © 2024 Kinooo. All rights reserved.
//

#import "CacheUsers.h"
#import "ImConfig.h"
#import "react-native-config/RNCConfig.h"

#define HeaderAuthKey @"X-IM-SDK-AUTH-KEY"

@interface CacheUsers() {
    NSDictionary *listUsers;
    NSDictionary *listCustomerServiceAndChatbot;
    NSString *apiUrl;
    NSString *authKey;
}

@property (nonatomic, readwrite, strong) NSDictionary *listUsers;
@property (nonatomic, readwrite, strong) NSDictionary *listCustomerServiceAndChatbot;
@property (nonatomic, strong) NSString *apiUrl;
@property (nonatomic, strong) NSString *authKey;

@end

@implementation CacheUsers

@synthesize listUsers = _listUsers;

@synthesize listCustomerServiceAndChatbot = _listCustomerServiceAndChatbot;

@synthesize apiUrl = _apiUrl;

@synthesize authKey = _authKey;

-(void) viewDidLoad {
    [super viewDidLoad];
}

+(instancetype) initWithCacheUsers {
    static CacheUsers *cu = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        cu = [[CacheUsers alloc] init];
    });
    
    return cu;
}

-(instancetype)init {
    self = [super init];
    if (self) {
        _listUsers = [[NSMutableDictionary alloc] init];
        _listCustomerServiceAndChatbot = [[NSMutableDictionary alloc] init];
        _apiUrl = [RNCConfig envFor:@"API_URL"];
        _authKey = [RNCConfig envFor:@"API_AUTH_KEY"];
    }
    
    return self;
}

-(NSDictionary *)getUser:(NSString *)accId {
    if ([_listCustomerServiceAndChatbot objectForKey:accId] != nil) {
        return nil;
    }
    
    return [_listUsers objectForKey:accId];
}

-(void)setListCustomerServiceAndChatbot:(NSDictionary *)listCustomerServiceAndChatbot {
    if (_listCustomerServiceAndChatbot.count == 0) {
        _listCustomerServiceAndChatbot = listCustomerServiceAndChatbot;
    }
}

-(NSString *)getCustomerServiceOrChatbot:(NSString *)accId {
    return [_listCustomerServiceAndChatbot objectForKey:accId];
}

-(void)fetchUsers:(NSArray<NSString *> *)accIds completion:(NIMFetchUsersHandle)completion {
    if (_apiUrl == nil || _authKey == nil) {
        NSMutableDictionary *details = [[NSMutableDictionary alloc] init];
        [details setObject:@"CacheUser fetchUsers" forKey:NSLocalizedDescriptionKey];
        completion(nil, [NSError errorWithDomain:@"api url or auth key not found" code:404 userInfo:details]);
        return;
    }
    
    NSLog(@"fetchUsers: %@, %@, %@",accIds, _apiUrl, _authKey);
//    
//    NSMutableString *endpoint = [NSMutableString stringWithFormat:@"%@/api/v1/client/im-sdk/users", _apiUrl];
//    for(int i = 0; i < [accIds count]; i++) {
//        if (i == 0) {
//            [endpoint appendFormat:@"?accId=%@", [accIds objectAtIndex:i]];
//            continue;
//        }
//        
//        [endpoint appendFormat:@"&accId=%@", [accIds objectAtIndex:i]]
//    }
    
    NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"%@/api/v1/client/im-sdk/users", _apiUrl]];
 
//    NSURLComponents *components = [[NSURLComponents alloc] initWithURL:url resolvingAgainstBaseURL:NO];
//    NSMutableArray *queryItems = [NSMutableArray arrayWithArray:components.queryItems];
//    for(NSString *accId in accIds) {
//        NSURLQueryItem *queryItem = [[NSURLQueryItem alloc] initWithName:@"accIds" value:accId];
//        [queryItems addObject:queryItem];
//    }
//    
//    components.queryItems = queryItems;
//    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    [request setHTTPMethod:@"POST"];
    [request addValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request addValue:_authKey forHTTPHeaderField:HeaderAuthKey];
    
    NSMutableDictionary *body = [[NSMutableDictionary alloc] init];
    [body setObject:accIds forKey:@"accIds"];
    NSError *error;
    NSData *bodyData = [NSJSONSerialization dataWithJSONObject:body options:0 error:&error];
    if (error != nil) {
        NSLog(@"fetchUsers: %@", error);
        completion(nil, error);
        return;
    }
    
    request.HTTPBody = bodyData;
    
    NSURLSession *session = [NSURLSession sharedSession];
    NSURLSessionDataTask *dataTask = [session dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *reponse, NSError *error) {
        if (error != nil) {
            NSLog(@"fetchUsers error: %@",error);
            completion(nil, error);
            return;
        }
        
        NSLog(@"fetchUsers data: %@", data);
        if (data == nil ) {
            completion(nil, nil);
            return;
        }
        
        NSError *parseDataErr = nil;
        NSDictionary *responseData = [NSDictionary dictionaryWithDictionary:[NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:&error]];
        
        if (parseDataErr != nil) {
            NSLog(@"fetchUsers parseDataErr: %@", parseDataErr);
            completion(nil, parseDataErr);
            return;
        }
        
        NSLog(@"fetchUsers responseData: %@", responseData);
        
        NSMutableDictionary *updateListUserInfo = self.listUsers ? [self.listUsers mutableCopy] : [[NSMutableDictionary alloc] init];
        
        for(NSString *accId in [responseData allKeys]) {
            if ([responseData objectForKey:accId] != nil) {
                [updateListUserInfo setObject:[responseData objectForKey:accId] forKey:accId];
            }
        }
        
        self.listUsers = updateListUserInfo;
        
        completion(responseData,nil);
    }];
    
    [dataTask resume];
}

@end
