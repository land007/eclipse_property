 //
//  AppDelegate.m
//  JJHahaPic
//
//  Created by diaochunmeng on 13-11-19.
//  Copyright (c) 2013年 JJ. All rights reserved.
//

#import "AppDelegate.h"

#import "UMSAgent.h"
#import "CUtility.h"
#import "JJHahaMainTableViewContronller.h"
#import "JJHahaMainViewContronller.h"
#import "CDBMigrationController.h"
#import "JJHahaGuideViewContronller.h"
#import "CustomNaiViewController.h"
#import "NSString+MD5.h"
#import "Engine+PushService.h"
#import "JJSavingState.h"
#import "MBProgressHUDManager.h"
#import "NSDictionary+QueryString.h"
#import "JJNotificationBar.h"
#import "CRemoteNotification.h"
#import "CDatabase+PushService.h"
#import "CacheDefines+JJHahaPic.h"
#import "JJHaHaPicCheckVersion.h"
#import "JJHahaPicError.h"
#import "UIAlertViewController.h"
#import "JJHaHaPicMenuViewController.h"
#import "MHFacebookImageViewer.h"
#import "JJHahaDetailViewContronller.h"
#import "JJNotificationBarInfoManager.h"
#import "JJLoadingPageViewController.h"
#import "Engine+JJHahaLoadingPage.h"
#import "Engine+PushService.h"
#import "MWPhotoBrowser.h"
#import "Engine+JJHahaDetailViewContronller.h"
#import "WebServiceEngine.h"
#import "JJHaHaPicSharedViewController.h"
#import "CDatabase+JJHaHaPicSubjectDetailViewController.h"
#import "CDatabase+JJHahaMainTableViewContronller.h"
#import "LandscapeNavigationController.h"
#import "WebServiceEngine+JJHahaPicClassifyViewController.h"


@implementation UINavigationController(InterfaceOrientations)

-(NSUInteger)supportedInterfaceOrientations
{
    if([self isKindOfClass:[LandscapeNavigationController class]])
        return [self.topViewController supportedInterfaceOrientations];
    else
        return UIInterfaceOrientationMaskPortrait;
}

- (BOOL)shouldAutorotate
{
    return YES;
}

- (BOOL)shouldAutomaticallyForwardRotationMethods
{
    return NO;
}

@end

@interface WBBaseResponse ()
- (void)debugPrint;
@end

@interface AppDelegate()

@end

@implementation AppDelegate

@synthesize window = _window;
@synthesize viewController = _viewController;
@synthesize tokenRequest = _tokenRequest;
@synthesize mhFacebookImageViewcurrentCell;
//@synthesize pushNotificationQueue = _pushNotificationQueue;

- (void)dealloc
{
    [_window release];
    [_viewController release];
    //[_pushNotificationQueue release];
    
    [_tencentOAuth release];
    //[_tencentWeiboApi release];
    [_jjOAuth release];
    
    [super dealloc];
}

- (id)init
{
    if (self = [super init])
    {
        //_pushNotificationQueue = [[CLineraQueue alloc] init];
    }
    return self;
}

//- (NSUInteger)application:(UIApplication *)application supportedInterfaceOrientationsForWindow:(UIWindow *)window
//{
//    return UIInterfaceOrientationMaskAll;
//}

-(void)initUserDefault
{
    [[CDBMigrationController sharedInstance] migrate];
    
    NSString *currentTheme = [[NSUserDefaults standardUserDefaults] objectForKey:kThemeSelectedKey];
    if (currentTheme && [currentTheme length] > 0) {
        [[CThemeManager sharedThemeManager] setCurrentTheme:currentTheme];
    } else {
        [[CThemeManager sharedThemeManager] setCurrentTheme:kThemeDefault];
    }
    
    [[UIApplication sharedApplication] setStatusBarHidden:NO];
    [UIApplication sharedApplication].applicationIconBadgeNumber = 0;
}

-(void)initUMAgent
{
    [UMSAgent bindUserIdentifier:[[CUtility getCFUUID] md5HexDigest]];
#if (DISTRIBUTE_APP_STORE)
    [UMSAgent startWithAppKey:@"a93fb0f83cf707743758248345d8340e" ReportPolicy:REALTIME ServerURL:@"http://analysis.app.jj.cn/analysis/index.php?"];
#elif(DISTRIBUTE_PB)
    [UMSAgent startWithAppKey:[CUtility statisticsID] ReportPolicy:REALTIME ServerURL:@"http://analysis.app.jj.cn/analysis/index.php?"];
#else
    [UMSAgent startWithAppKey:@"bbde0fa8ee5b63df4dde75038c108663" ReportPolicy:REALTIME ServerURL:@"http://analysis.app.jj.cn/analysis/index.php?"];
#endif
    
    [UMSAgent setIsLogEnabled:YES];
}

-(void)initRemoteNotification:(UIApplication*)application options:(NSDictionary*)launchOptions
{
    //判断是否由远程消息通知触发应用程序启动
    if([launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey]!=nil)
    {
        //获取应用程序消息通知标记数（即小红圈中的数字）
        int badge = [UIApplication sharedApplication].applicationIconBadgeNumber;
        if(badge>0)
        {
            //如果应用程序消息通知标记数（即小红圈中的数字）大于0，清除标记。
            badge = 0;
            //清除标记。清除小红圈中数字，小红圈中数字为0，小红圈才会消除。
            [UIApplication sharedApplication].applicationIconBadgeNumber = badge;
        }
        
        //推送通知
        NSDictionary* userInfo = [launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
        [self performSelector:@selector(application:didReceiveRemoteNotification:) withObject:application withObject:userInfo];
    }
    //清除所有历史推送信息
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber: 1];
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber: 0];
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
}

-(void)initRegistRemoteNotification
{
    //消息推送注册
    if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"8.0")) {
        [[UIApplication sharedApplication] registerUserNotificationSettings:[UIUserNotificationSettings
                                                                             settingsForTypes:(UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge)
                                                                             categories:nil]];
        
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    }else {
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:UIRemoteNotificationTypeSound|UIRemoteNotificationTypeAlert|UIRemoteNotificationTypeBadge];
    }
}

-(void)initThirdPartySdks
{
    //第三方登录、分享
    //sina微博
    [WeiboSDK registerApp:kSinaWeiBoAppKey];
    //QQ
    _tencentOAuth = [[TencentOAuth alloc] initWithAppId:QQAppKey andDelegate:self];
    //JJ
    _jjOAuth = [[JJOAuthManager alloc]initWithDelegate:self];
    
    //腾讯微博
    //去掉腾讯微博分享操作
//    _tencentWeiboApi = [[WeiboApi alloc]initWithAppKey:TencentWeiboAppKey andSecret:TencentWeiboAppSECRET andRedirectUri:TencentWeiboRedirectURI];
//    NSString* accessToken = [[NSUserDefaults standardUserDefaults] objectForKey:kTencentWeiBoAccessToken];
//    NSString* openId = [[NSUserDefaults standardUserDefaults] objectForKey:kTencentWeiBoOpenId];
//    NSString* refreshToken = [[NSUserDefaults standardUserDefaults] objectForKey:kTencentWeiBoRefreshToken];
//    if (accessToken.length > 0 && openId.length > 0 && refreshToken.length > 0) {
//        //已经绑定过，则需刷新token
//        _tencentWeiboApi.refreshToken = refreshToken;
//        [_tencentWeiboApi refreshAuthWithDelegate:self];
//    }
    
    //注册微信
    [WXApi registerApp:kWeixinAppKey];
}

-(void)refreshLoadingPage
{
    //[[WebServiceEngine sharedInstance] performSelector:@selector(getLoadingPage_block) withObject:nil afterDelay:5];
    
    [[JJLoadingPageViewController getInstance] showViewControllerAnimated:NO completion:^(BOOL finished) {
        
    }];
    
    [[WebServiceEngine sharedInstance] getLoadingPage_block:^{
        [[JJLoadingPageViewController getInstance] updateImage];
    }];
}

-(void)updateDBName
{
    NSArray  *paths                 = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory	= [paths objectAtIndex:0];
	NSString *dbFilePathOld         = [documentsDirectory stringByAppendingPathComponent:@"SohuNews.sqlite"];
    NSString *dbFilePath            = [documentsDirectory stringByAppendingPathComponent:DB_FILE_NAME];
    
    NSError* error;
    NSFileManager *fileMgr = [NSFileManager defaultManager];
    [fileMgr moveItemAtPath:dbFilePathOld toPath:dbFilePath error:&error];
    [fileMgr moveItemAtPath:[NSString stringWithFormat:@"%@-shm", dbFilePathOld] toPath:[NSString stringWithFormat:@"%@-shm", dbFilePath] error:&error];
    [fileMgr moveItemAtPath:[NSString stringWithFormat:@"%@-wal", dbFilePathOld] toPath:[NSString stringWithFormat:@"%@-wal", dbFilePath] error:&error];
}

- (BOOL)application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
//#if T ARGET_IPHONE_SIMULATOR
//    // where are you?
//    NSLog(@"Documents Directory: %@", [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject]);
//#endif
    
    [self updateDBName];
    [self initUserDefault];
    
    //检测版本
    [JJHaHaPicCheckVersion checkVersionIsAuto:YES];
    //日志
#if TARGET_OS_IPHONE
#if DEBUG_MODE
    [self redirectNSLogToDocumentFolder];
#endif
#endif
    
    UINavigationController* mainViewController = [JJHahaMainViewContronller getInstanceWithNav];
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    [self.window setRootViewController:mainViewController];
    self.window.backgroundColor = [UIColor whiteColor];
    [self.window makeKeyAndVisible];
    
    [self initRegistRemoteNotification];
    [self initRemoteNotification:application options:launchOptions];
    [self initUMAgent];
    [self refreshLoadingPage];
    [self initThirdPartySdks];
    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    NSMutableArray* array = [JJSavingState sharedSavingState].registerProtocolArray;
    NSMutableArray* bodyArray = [JJSavingState sharedSavingState].registerBodyArray;
    if(array.count>0 && bodyArray.count>0)
    {
        for(NSInteger i=array.count-1; i>=0; i--)
        {
            NSString* string = (NSString*)[array objectAtIndex:i];
            NSDictionary* dic = (NSDictionary*)[bodyArray objectAtIndex:i];
            [[WebServiceEngine sharedInstance] repeateRegisterOrDeRegister:string dic:dic];
            return;
        }
    }
}

- (void)applicationWillEnterForeground:(UIApplication*)application
{
    //清除所有历史推送信息
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber: 1];
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber: 0];
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
    
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    [[UIApplication sharedApplication] setStatusBarHidden:NO];
 
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
//    更新分类界面
    [[WebServiceEngine sharedInstance] getClassifyListBlock];
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

-(void)application:(UIApplication*)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken
{
    NSString *token = [NSString stringWithFormat:@"%@", deviceToken];
    //获取终端设备标识，这个标识需要通过接口发送到服务器端，服务器端推送消息到APNS时需要知道终端的标识，APNS通过注册的终端标识找到终端设备。
    token = [token stringByTrimmingCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@"<>"]];
    token = [token stringByReplacingOccurrencesOfString:@" " withString:@""];
    NSLog(@"My token is:%@", token);
    
    if(self.tokenRequest.length==0 && token.length>0 && [JJSavingState sharedSavingState].pushTokenUploaedDate.length==0 && [JJSavingState sharedSavingState].isPushPosts)
    {
        self.tokenRequest = [[WebServiceEngine sharedInstance] addDeviceRequest:token showError:NO];
    }
}

-(void)application:(UIApplication*)application didFailToRegisterForRemoteNotificationsWithError:(NSError*)error
{
//    NSString *error_str = [NSString stringWithFormat: @"%@", error];
//    NSLog(@"Failed to get token, error:%@", error_str);
}

/**
 * 递归遍历view的所有子view  直到找到UIActionSheet
 */
- (NSArray *)allSubViews:(UIView *)v
{
    NSMutableArray *tmp = [NSMutableArray array];
    for (UIView *v1 in v.subviews) {
        [tmp addObject:v1];
        if ([v1 isKindOfClass:[UIActionSheet class]]) {
            return tmp;
        }
        [tmp addObjectsFromArray:[self allSubViews:v1]];
    }
    return tmp;
}

-(UIViewController*)topViewController
{
    return [self topViewController:[UIApplication sharedApplication].keyWindow.rootViewController];
}

- (UIViewController *)topViewController:(UIViewController *)rootViewController
{
    if (rootViewController.presentedViewController == nil) {
        return rootViewController;
    }
    
    if ([rootViewController.presentedViewController isMemberOfClass:[UINavigationController class]]) {
        UINavigationController *navigationController = (UINavigationController *)rootViewController.presentedViewController;
        UIViewController *lastViewController = [[navigationController viewControllers] lastObject];
        return [self topViewController:lastViewController];
    }
    
    UIViewController *presentedViewController = (UIViewController *)rootViewController.presentedViewController;
    return [self topViewController:presentedViewController];
}

- (UINavigationController*)rootNavigationController{
    UIViewController* rootViewController = self.window.rootViewController;
    UINavigationController* rootNavigationController = nil;
    if ([rootViewController isKindOfClass:[UINavigationController class]]) {
        rootNavigationController = (UINavigationController*)rootViewController;
    }else {
        rootNavigationController = rootViewController.navigationController;
    }
    
    return rootNavigationController;
}

-(void)pushNodeNow:(NSString*)aNode
{
    id top = [self topViewController];
    if(aNode.length>0 && [top isKindOfClass:[UINavigationController class]])
    {
        UINavigationController* nav = (UINavigationController*)top;
        JJHahaDetailViewContronller* jdvc = [[JJHahaDetailViewContronller alloc] initWithNid:aNode];
        [nav pushViewController:jdvc animated:YES];
        [jdvc release];
    }
}

-(void)application:(UIApplication*)application didReceiveRemoteNotification:(NSDictionary*)userInfo
{
//    id top = [self topViewController];
//    if([top isKindOfClass:[UINavigationController class]])
//    {
//        UINavigationController* nav = (UINavigationController*)top;
//        UIViewController* aaa = [[UIViewController alloc] init];
//        aaa.view.backgroundColor = [UIColor lightGrayColor];
//        [nav pushViewController:aaa animated:YES];
//        [aaa release];
//    }
//    return;
    
//	if(userInfo)
//    {
//        [_pushNotificationQueue checkIn:userInfo];
//        // 收到了通知 发个notify
//        [[NSNotificationCenter defaultCenter] postNotificationName:kNotifyDidReceive object:nil];
//	}
    
    //只会在后台挂起(UIApplicationStateBackground)的时候运行，即程序后台挂起的时候接收到了通知
//	if (!(application.applicationState == UIApplicationStateActive)) {
//        //[[SNAPNSHandler sharedInstance] handleReciveNotify];
//	}
    //程序运行中接收到了通知
//	else
    {
		NSDictionary* apsDic = [userInfo objectForKey:@"aps"];
        NSDictionary* alertDic = [apsDic objectForKey:@"alert"];
        if(![alertDic isKindOfClass:[NSDictionary class]])
            return;
        
        //群推：升级,新帖，网页
        NSString* bak = [userInfo objectForKey:@"bak"];
        NSString* para = [userInfo objectForKey:@"bak2"];
        NSString* date = [userInfo objectForKey:@"t"];
		NSString* alertStr = [alertDic objectForKey:@"body"];
        
        //有人回复了你的帖子
        NSDictionary* expand = [apsDic objectForKey:@"expand"];
        NSString* time = [apsDic objectForKey:@"time"];
        NSString* type = [expand objectForKey:@"type"];
        
        NSMutableDictionary* args = [[[expand objectForKey:@"args"] mutableCopy] autorelease];
        
        //恢复原有字段
        NSString* typeValue = [args objectForKey:@"type"];
        if([typeValue isEqualToString:@"s"])
            [args setObject:@"special_topic" forKey:@"type"];
        else if([typeValue isEqualToString:@"a"])
            [args setObject:@"article" forKey:@"type"];
        else if([typeValue isEqualToString:@"v"])
            [args setObject:@"video" forKey:@"type"];
        
        NSString* cid = [args objectForKey:@"cid"];
        NSString* nid = [args objectForKey:@"nid"];
        NSString* t = [args objectForKey:@"type"];
        
		if([alertStr isKindOfClass:[NSString class]] &&  alertStr.length>0)
        {
//            for (UIWindow *window in [UIApplication sharedApplication].windows)
//            {
//                if([window isKindOfClass:NSClassFromString(@"_UIAlertOverlayWindow")])
//                {
//                    NSArray *arr =[self allSubViews:window];
//                    UIActionSheet *actionSheet = nil;
//                    for (UIView *v in arr) {
//                        if ([v isKindOfClass:[UIActionSheet class]])
//                        {
//                            actionSheet = (UIActionSheet *)v;
//                            break ;
//                        }
//                    }
//                    [actionSheet dismissWithClickedButtonIndex:actionSheet.cancelButtonIndex animated:YES];
//                }
//            }
            
            if([bak isKindOfClass:[NSString class]] && bak.length>0)
            {
                //保存通知到数据库
                CRemoteNotification* obj = [[CRemoteNotification alloc] init];
                obj.title = alertStr;
                obj.date = date;
                obj.url = bak;
                obj.parameter = para;
                [[CDBManager currentDataBase] saveRemoteNotification:obj];
                [obj release];
                
                //通知
                CNotificationBarInfo* info = [CNotificationBarInfo GetFlashInfoWithTitle:alertStr];
                info.jumpUrl = bak;
                info.para = para;
                info.pushType = pushTypeSystemMessage;
                if ([bak hasPrefix:@"jjhahaupdate://"])
                {
                    info.type = EPersistent;
                }
                else
                {
                //  本地推送
                    /*
                    [[JJSavingState sharedSavingState] setIsCommentReply:YES];
                    [[JJSavingState sharedSavingState] setIsSystemMessage:YES];
                     */
                    [JJSavingState sharedSavingState].systemMessageBadgeNum+=1;
                    [JJSavingState saveSavingState];
                    [[NSNotificationCenter defaultCenter] postNotificationName:MessagepromptNotification object:nil];
                }
                [[JJNotificationBarInfoManager sharedInstance] addNotificationInfo:info];
                //
            }
            else if([type isKindOfClass:[NSString class]] && [nid isKindOfClass:[NSString class]])
            {
                NSString* url = [NSString stringWithFormat:@"%@://nid=%@", type, nid];
                if(cid.length>0)
                    url = [NSString stringWithFormat:@"%@&cid=%@", url, cid];
                if(t.length>0)
                    url = [NSString stringWithFormat:@"%@&type=%@", url, t];
                
                //保存通知到数据库
                CRemoteNotification* obj = [[CRemoteNotification alloc] init];
                obj.title = alertStr;
                obj.date = time;
                obj.url = url;
                obj.parameter = para;
                [[CDBManager currentDataBase] saveRemoteNotification:obj];
                [obj release];
                
                //通知
                CNotificationBarInfo* info = [CNotificationBarInfo GetFlashInfoWithTitle:alertStr];
                info.jumpUrl = url;
                info.para = para;
                info.pushType = pushTypeSystemMessage;
                [[JJNotificationBarInfoManager sharedInstance] addNotificationInfo:info];
                
            // 有人回复你的评论
//                [[JJSavingState sharedSavingState] setIsSystemMessage:YES];
                [JJSavingState sharedSavingState].systemMessageBadgeNum+=1;
                [JJSavingState saveSavingState];
                [[NSNotificationCenter defaultCenter] postNotificationName:MessagepromptNotification object:nil];
            }
		}
        
        //如果推送的时候帖子信息的话
//        NSDictionary * dictionary = [CUtility dictionaryFromQuery:bak usingEncoding:NSUTF8StringEncoding];
//        if([[dictionary objectForKey:@"_protocol_"] isEqualToString:@"jjhahatip"])
//        {
//            NSString* nid = [dictionary objectForKey:@"nid"];
//            if(nid.length>0)
//            {
//                CGFloat delay = 0;
//                if([[JJHaHaPicMenuViewController shareInstance].view superview]!=nil)
//                {
//                    delay = 0.5f;
//                    [[JJHaHaPicMenuViewController shareInstance]  hideViewControllerAnimated:YES completion:^(BOOL finished) {
//                    }];
//                }
//                else if([[UIAlertViewController shareInstanceWithDetailReportNid:nil].view superview]!=nil)
//                {
//                    delay = 0.5f;
//                    [[UIAlertViewController shareInstanceWithDetailReportNid:nil] dismissNow];
//                }
//                else if(self.mhFacebookImageViewcurrentCell!=nil)
//                {
//                    delay = 0.5f;
//                    [mhFacebookImageViewcurrentCell close:nil];
//                }
//                //push after delay
//                [self performSelector:@selector(pushNodeNow:) withObject:nid afterDelay:delay];
//            }
//        }
	}
}

#pragma mark - share
- (BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url
{
    NSString* scheme = [url scheme];
    if ([scheme isEqualToString:kSinaWeiBoScheme]) {
        //sina微博
        return [WeiboSDK handleOpenURL:url delegate:self];
    }else if ([scheme isEqualToString:QQNewScheme] ||
              [scheme isEqualToString:QQOldScheme]){
        return [QQApiInterface handleOpenURL:url delegate:self] || [TencentOAuth HandleOpenURL:url];
//    }else if ([scheme isEqualToString:TencentWeiboScheme]){
//         return [self.tencentWeiboApi handleOpenURL:url];
    }else if ([scheme isEqualToString:kWeixinAppKey]){
        return  [WXApi handleOpenURL:url delegate:self];
    }
    else{
        return YES;
    }
}

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
    NSString* scheme = [url scheme];
    if ([scheme isEqualToString:kSinaWeiBoScheme]) {
        //sina微博
        return [WeiboSDK handleOpenURL:url delegate:self];
    }else if ([scheme isEqualToString:QQNewScheme] ||
              [scheme isEqualToString:QQOldScheme]){
//        NSString* host = [url host];
//        if ([host isEqualToString:@"tauth.qq.com"]) {
//            return [TencentOAuth HandleOpenURL:url];
//        }else{
//            return [QQApiInterface handleOpenURL:url delegate:self];
//        }
        
        return [QQApiInterface handleOpenURL:url delegate:self] || [TencentOAuth HandleOpenURL:url];
//    }else if ([scheme isEqualToString:TencentWeiboScheme]){
//        return [self.tencentWeiboApi handleOpenURL:url];
    }else if ([scheme isEqualToString:kWeixinAppKey]){
        return  [WXApi handleOpenURL:url delegate:self];
    }
    else{
        return YES;
    }
}

// 分享成功统一回调处理
-(void)handleShareSuccessed
{
    CRollingNode *node = [JJHaHaPicSharedViewController shareInstance].shareData;
    node.shareNum = [NSString stringWithFormat:@"%d",[node.shareNum integerValue]+1];
    if([node.type isEqualToString:@"special_topic"])
        [[CDBManager currentDataBase] saveCSubjectNode:node];
    else if([node.type isEqualToString:@"video"])
        [[CDBManager currentDataBase] saveCSubjectNode:node];
    else
        [[CDBManager currentDataBase] saveCRollingNode:node];
    [[NSNotificationCenter defaultCenter] postNotificationName:JJRollingNodeShareChangedNotification object:node userInfo:nil];
    [[WebServiceEngine sharedInstance] postShareWithNode:node];
}

- (void)onReq:(BaseResp *)req
{
    
}

- (void)onResp:(BaseResp *)resp{
    if([resp isKindOfClass:[SendMessageToWXResp class]])
    {
        if (WXErrCodeUserCancel == resp.errCode){
            return;
        }
        //NSLog(@"resp.errCode = %d", resp.errCode);
        
        //微信分享返回处理
        NSString* strMessage = nil;
        if (resp.errCode != 0) {
            //strMessage = [NSString stringWithFormat:@"分享失败 errorCode:%d", resp.errCode];
            //strMessage = @"分享失败"
            strMessage = NSLocalizedStringFromTable(@"Toast_Share_Fail",@"InfoPlist", @"");
        }else{
            strMessage = NSLocalizedStringFromTable(@"Toast_Share_Success",@"InfoPlist", @"");
            [self handleShareSuccessed];
        }
        [MBProgressHUDManager totastOnView:nil withMessage:strMessage];
    }
    else if([resp isKindOfClass:[SendMessageToQQResp class]]){
        //QQ分享返回处理
        SendMessageToQQResp* sendResp = (SendMessageToQQResp*)resp;
        if (-4 == [sendResp.result intValue]){
            return;
        }
        //NSLog(@"[sendResp.result intValue] = %d", [sendResp.result intValue]);
        
        NSString* strMessage = nil;
        if ([sendResp.result intValue] != 0) {
            strMessage = NSLocalizedStringFromTable(@"Toast_Share_Fail",@"InfoPlist", @"");
        }else{
            strMessage = NSLocalizedStringFromTable(@"Toast_Share_Success",@"InfoPlist", @"");
            [self handleShareSuccessed];
        }
        
        [MBProgressHUDManager totastOnView:nil withMessage:strMessage];
    }
}


#pragma mark - QQ
- (void)isOnlineResponse:(NSDictionary *)response
{
    [self handleShareSuccessed];
    /*
    CRollingNode *node = [JJHaHaPicSharedViewController shareInstance].shareData;
    node.shareNum = [NSString stringWithFormat:@"%d",[node.shareNum integerValue]+1];
    [[NSNotificationCenter defaultCenter] postNotificationName:JJRollingNodeShareChangedNotification object:node userInfo:nil];
    [[WebServiceEngine sharedInstance] postShareWithNode:node];
     */
}

- (void)tencentDidLogin{
    NSDictionary* dictParams = [NSDictionary dictionaryWithObjectsAndKeys:self.tencentOAuth.accessToken,@"access_token",
                                self.tencentOAuth.openId,@"uid", [NSString stringWithFormat:@"%d", enAuthType_QQ],kAuthTypeKey,nil];
    
    //[JJHahaPicShare saveQQToken:self.tencentOAuth];
    
    [[NSNotificationCenter defaultCenter] postNotificationName:didJJHahaPicAuthorizeRequestBeginNotification object:nil userInfo:dictParams];
}

- (void)tencentDidNotLogin:(BOOL)cancelled{
    
}

- (void)tencentDidNotNetWork{
    NSError* error = [NSError errorWithDomain:kJJHahaPicErrorDomain code:-1 userInfo:[NSDictionary dictionaryWithObjectsAndKeys:NSLocalizedStringFromTable(@"Toast_Net_Error",@"InfoPlist", @""),kJJHahaPicErrorUserInfoMessageKey, nil]];
    [[NSNotificationCenter defaultCenter] postNotificationName:didJJHahaPicAuthorizeRequestFailNotification object:nil userInfo:[NSDictionary dictionaryWithObjectsAndKeys:error,kJJHahaPicErrorKey, [NSString stringWithFormat:@"%d", enAuthType_QQ],kAuthTypeKey,nil]];
}

- (void)tencentOAuth:(TencentOAuth *)tencentOAuth doCloseViewController:(UIViewController *)viewController{
    
}

#pragma mark - tencent weibo
//WeiboAuthDelegate
//- (void)DidAuthFinished:(WeiboApi *)wbobj
//{
//    [JJHahaPicShare saveTencentWeiboToken:wbobj];
//}
//
//- (void)DidAuthCanceled:(WeiboApi *)wbobj
//{
//    
//}

- (void)DidAuthFailWithError:(NSError *)error
{
    //NSString *str = [[NSString alloc] initWithFormat:@"get token error, errcode = %@",error.userInfo];
}

//- (void)DidAuthRefreshed:(WeiboApi *)wbobj
//{
//    [JJHahaPicShare saveTencentWeiboToken:wbobj];
//}

- (void)DidAuthRefreshFail:(NSError *)error
{
    
}

//WeiboRequestDelegate
- (void)didReceiveRawData:(NSData *)data reqNo:(int)reqno{
    //NSString *strResult = [[NSString alloc] initWithBytes:[data bytes] length:[data length] encoding:NSUTF8StringEncoding];
    
    NSError *parserError;
    NSDictionary *dictData = [NSJSONSerialization JSONObjectWithData:data options:0 error:&parserError];
    
    if (dictData == nil) {
        // try and decode the response body as a query string instead
        NSString *responseString = [[[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding]autorelease];
        dictData = [NSDictionary dictionaryWithFormEncodedString:responseString];
    }
    
    NSString* strCode = [dictData objectForKey:@"ret"];
    NSString* strMessage = nil;
    if (strCode && [strCode integerValue] >= 0) {
        strMessage = NSLocalizedStringFromTable(@"Toast_Share_Success",@"InfoPlist", @"");
        [self handleShareSuccessed];
    }else{
        strMessage = NSLocalizedStringFromTable(@"Toast_Share_Fail",@"InfoPlist", @"");
    }
    [MBProgressHUDManager totastOnView:nil withMessage:strMessage];
}

- (void)didFailWithError:(NSError *)error reqNo:(int)reqno{
    
}

- (void)didNeedRelogin:(NSError *)error reqNo:(int)reqno{
    
}

#pragma mark - sina weibo
- (void)didReceiveWeiboRequest:(WBBaseRequest *)request
{
    if ([request isKindOfClass:WBProvideMessageForWeiboRequest.class]){
    }
}

- (void)didReceiveWeiboResponse:(WBBaseResponse *)response
{
    if ([response isKindOfClass:WBSendMessageToWeiboResponse.class]){
        //SDK分享时的返回处理
        NSString* strMessage = nil;
        if (response.statusCode >= WeiboSDKResponseStatusCodeSuccess) {
            strMessage = NSLocalizedStringFromTable(@"Toast_Share_Success",@"InfoPlist", @"");
             [self handleShareSuccessed];
        }else{
            strMessage = NSLocalizedStringFromTable(@"Toast_Share_Fail",@"InfoPlist", @"");
        }
        [MBProgressHUDManager totastOnView:nil withMessage:strMessage];
    }
    else if ([response isKindOfClass:WBAuthorizeResponse.class]){
        //第三方登录或绑定时返回
        if (WeiboSDKResponseStatusCodeSuccess == response.statusCode) {
            NSDictionary* dictParams = [NSDictionary dictionaryWithObjectsAndKeys:[(WBAuthorizeResponse *)response accessToken],@"access_token",[(WBAuthorizeResponse *)response userID],@"uid", [NSString stringWithFormat:@"%d", enAuthType_SinaWeibo],kAuthTypeKey, nil];
            
            //[JJHahaPicShare saveSinaWeiboToken:(WBAuthorizeResponse *)response];
            
            [[NSNotificationCenter defaultCenter] postNotificationName:didJJHahaPicAuthorizeRequestBeginNotification object:nil userInfo:dictParams];
        }else if (WeiboSDKResponseStatusCodeUserCancel == response.statusCode){
            
        }else{
            //失败
            NSError* error = [NSError errorWithDomain:kJJHahaPicErrorDomain code:response.statusCode userInfo:[NSDictionary dictionaryWithObjectsAndKeys:NSLocalizedStringFromTable(@"Toast_Auth_Fail",@"InfoPlist", @""),kJJHahaPicErrorUserInfoMessageKey, nil]];
            [[NSNotificationCenter defaultCenter] postNotificationName:didJJHahaPicAuthorizeRequestFailNotification object:nil userInfo:[NSDictionary dictionaryWithObject:error forKey:kJJHahaPicErrorKey]];
        }
    }
}

- (void)request:(WBHttpRequest *)request didFinishLoadingWithResult:(NSString *)result
{
}

- (void)request:(WBHttpRequest *)request didFailWithError:(NSError *)error;
{
    
}

- (void)request:(WBHttpRequest *)request didReceiveResponse:(NSURLResponse *)response{
    
}

- (void)request:(WBHttpRequest *)request didFinishLoadingWithDataResult:(NSData *)data{
    //一键分享(http请求)时的返回处理
    
    NSError *parserError;
    NSDictionary *dictData = [NSJSONSerialization JSONObjectWithData:data options:0 error:&parserError];
    
    if (dictData == nil) {
        // try and decode the response body as a query string instead
        NSString *responseString = [[[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding]autorelease];
        dictData = [NSDictionary dictionaryWithFormEncodedString:responseString];
    }
    
    NSString* strMessage = nil;
    if ([dictData objectForKey:@"id"]) {
        strMessage = NSLocalizedStringFromTable(@"Toast_Share_Success",@"InfoPlist", @"");
         [self handleShareSuccessed];
    }else if ([dictData objectForKey:@"error"]){
        strMessage = NSLocalizedStringFromTable(@"Toast_Share_Fail",@"InfoPlist", @"");
        NSLog(@"sina share error dictData = %@", dictData);
    }
    if (strMessage) {
        [MBProgressHUDManager totastOnView:nil withMessage:strMessage];
    }
    
//    if ([[dictData objectForKey:@"result"] isEqualToString:@"true"]) {
//        [JJHahaPicShare resetSinaWeiboToken];
//        
//        NSDictionary* dictParams = [NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"%d", enAuthType_UnBindSinaWeibo],kAuthTypeKey, nil];
//        [[NSNotificationCenter defaultCenter] postNotificationName:didJJHahaPicAuthorizeRequestBeginNotification object:nil userInfo:dictParams];
//    }else{
//        //失败
//    }
}

#pragma mark - JJOAuthDelegate
- (void)didAuthorizeSuccess:(JJOAuthManager*)oauth{
    NSInteger authType = 0;
    if ([oauth.authData.appId isEqualToString:JJAUTH2_APP_KEY]) {
        authType = enAuthType_JJ;
    }else if ([oauth.authData.appId isEqualToString:QQAppKey]){
        authType = enAuthType_QQ;
    }else {
        return;
    }
    
    NSDictionary* dictParams = [NSDictionary dictionaryWithObjectsAndKeys:oauth.authData.accessToken,@"access_token",[NSString stringWithFormat:@"%d", authType],kAuthTypeKey,
                                oauth.authData.openId,@"uid", nil];
    NSLog(@"dictParams = %@", dictParams);
    [[NSNotificationCenter defaultCenter] postNotificationName:didJJHahaPicAuthorizeRequestBeginNotification object:nil userInfo:dictParams];
}

- (void)oauth:(JJOAuthManager*)oauth didAuthorizeFailed:(NSError*)error{
    NSError* errorNew = [NSError errorWithDomain:kJJHahaPicErrorDomain code:error.code userInfo:[NSDictionary dictionaryWithObjectsAndKeys:NSLocalizedStringFromTable(@"Toast_Auth_Fail",@"InfoPlist", @""),kJJHahaPicErrorUserInfoMessageKey, nil]];
    [[NSNotificationCenter defaultCenter] postNotificationName:didJJHahaPicAuthorizeRequestFailNotification object:nil userInfo:[NSDictionary dictionaryWithObject:errorNew forKey:kJJHahaPicErrorKey]];
}

- (void)didAuthorizeCancel:(JJOAuthManager*)oauth{
    
}

- (void)redirectNSLogToDocumentFolder
{
    //如果已经连接Xcode调试则不输出到文件
    if(isatty(STDOUT_FILENO)) {
        return;
    } 
    
    UIDevice *device = [UIDevice currentDevice];
    if([[device model] hasSuffix:@"Simulator"]){ //在模拟器不保存到文件中
        return;
    }
    
    //将NSlog打印信息保存到Document目录下的Log文件夹下
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *logDirectory = [[paths objectAtIndex:0] stringByAppendingPathComponent:@"Log"];
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL fileExists = [fileManager fileExistsAtPath:logDirectory];
    if (!fileExists) {
        [fileManager createDirectoryAtPath:logDirectory  withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    NSDateFormatter *formatter = [[[NSDateFormatter alloc] init]autorelease];
    [formatter setLocale:[[[NSLocale alloc] initWithLocaleIdentifier:@"zh_CN"]autorelease]];
    [formatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"]; //每次启动后都保存一个新的日志文件中
    NSString *dateStr = [formatter stringFromDate:[NSDate date]];
    NSString *logFilePath = [logDirectory stringByAppendingFormat:@"/%@.log",dateStr];
    
    // 将log输入到文件
    freopen([logFilePath cStringUsingEncoding:NSASCIIStringEncoding], "a+", stdout);
    freopen([logFilePath cStringUsingEncoding:NSASCIIStringEncoding], "a+", stderr);
    
    //未捕获的Objective-C异常日志
    //NSSetUncaughtExceptionHandler (&UncaughtExceptionHandler);
}


@end
