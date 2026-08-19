# 聊天室队列管理

NetEase Yunxin IM supports chat room queue services, including initializing queues, adding \ updating queue elements, emptying queues and other operations.

The chat room queue is essentially a storage of a key-value pair, which can be used to store logic such as microphone bit management.

## Support platform

The development platform or framework applicable to this article is shown in the following table. For the interfaces involved, please refer to the following [relevant interface](#%E7%9B%B8%E5%85%B3%E6%8E%A5%E5%8F%A3)chapters:

| Android | iOS | macOS/Windows | Web/uni-app/applet | Node.js/Electron | Hongmeng | Flutter |
| ------- | --- | ------------- | ------------------ | ---------------- | -------- | ------- |
| ✔️️️️      | ✔️️️️  | ✔️️️️            | ✔️️️️                 | ✔️️️️               | ✔️️       | ✔️      |

## Technical principle

Permission is required to use the chat room queue service, which is controlled by `queueLevelMode`. If it is `ANY`, all chat room members can operate the queue. IF IT IS A `MANAGER`, ONLY THE CHAT ROOM CREATOR AND ADMINISTRATOR CAN OPERATE THE QUEUE.

[You can update the chat room information](https://doc.yunxin.163.com/messaging2/server-apis/jM0ODUxOTc?platform=server)through the server API and modify the chat room queue management permission mode.

## Prerequisites

在使用聊天室队列服务中的 API 前，需要先调用 `getChatroomQueueService` 方法获取聊天室队列服务类。

## Chat room queue-related event monitoring

Before operating the chat room queue, you can register to listen to the chat room queue-related events. After listening, you will receive the corresponding notification after the chat room queue management operation.

Related callback:

- **`onChatroomQueueOffered`**: New queue element callback in the chat room. After successfully adding an element to the chat room queue, the SDK will return the callback.
- **`onChatroomQueuePolled`**: The chat room takes out (removes) the queue element callback. Taking out (removing) elements from the chat room queue will trigger the callback.
- **`onChatroomQueueDropped`**: Chat room empty queue element callback. Emptying the elements in the chat room queue will trigger the callback.
- **`onChatroomQueuePartCleared`**: The chat room cleans up some queue elements to call back. The callback will be triggered when some elements in the chat room queue are cleaned up.
- **`onChatroomQueueBatchUpdated`**: Chat room batch update queue element callback. The callback will be triggered when the elements in the chat room queue are updated in batches.
- **`onChatroomQueueBatchOffered`**: Add queue elements to the chat room in batches to call back. The callback will be triggered when elements are added in batches in the chat room queue.

The sample code is as follows:

安卓

调用 [`addQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#addQueueListener) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
V2NIMChatroomQueueListener queueListener = new V2NIMChatroomQueueListener() {
    @Override
    public void onChatroomQueueOffered(V2NIMChatroomQueueElement element) {
        //新增队列元素
    }

    @Override
    public void onChatroomQueuePolled(V2NIMChatroomQueueElement element) {
        //删除队列元素
    }

    @Override
    public void onChatroomQueueDropped() {
        //队列被清空
    }

    @Override
    public void onChatroomQueuePartCleared(List<V2NIMChatroomQueueElement> elements) {
        //部分队列元素被清除
    }

    @Override
    public void onChatroomQueueBatchUpdated(List<V2NIMChatroomQueueElement> elements) {
        //批量更新队列元素
    }

    @Override
    public void onChatroomQueueBatchOffered(List<V2NIMChatroomQueueElement> elements) {
        //批量新增队列元素
    }
};
//设置队列服务监听可以在进入聊天室之前就设置
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
chatroomQueueService.addQueueListener(queueListener);

```

iOS

调用 [`addQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#addQueueListener) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
@interface MyQueueListener : NSObject<V2NIMChatroomQueueListener>
@end
@implementation MyQueueListener

- (void)onChatroomQueueOffered:(V2NIMChatroomQueueElement *)element
{
    // 聊天室新增队列元素
}
- (void)onChatroomQueuePolled:(V2NIMChatroomQueueElement *)element
{
    // 聊天室移除队列元素
}
- (void)onChatroomQueueDropped
{
    // 聊天室清空队列元素
}
- (void)onChatroomQueuePartCleared:(NSArray<V2NIMChatroomQueueElement *> *)elements
{
    // 聊天室清理部分队列元素
}
- (void)onChatroomQueueBatchUpdated:(NSArray<V2NIMChatroomQueueElement *> *)elements
{
    // 聊天室批量更新队列元素
}
- (void)onChatroomQueueBatchOffered:(NSArray<V2NIMChatroomQueueElement *> *)elements
{
    // 聊天室批量添加队列元素
}
@end
MyQueueListener *queueListener = [[MyQueueListener alloc] init];
//设置队列服务监听可以在进入聊天室之前就设置
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
[chatroomQueueService addQueueListener:queueListener];

```

macOS/Windows

调用 [`addQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#addQueueListener) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
V2NIMChatroomQueueListener listener;
listener.onChatroomQueueOffered = [](const V2NIMChatroomQueueElement& element) {
   // handle event
};
listener.onChatroomQueuePolled = [](const V2NIMChatroomQueueElement& element) {
   // handle event
};
listener.onChatroomQueueDropped = []() {
   // handle event
};
listener.onChatroomQueuePartCleared = [](const std::vector<V2NIMChatroomQueueElement>& keyValues) {
   // handle event
};
listener.onChatroomQueueBatchUpdated = [](const std::vector<V2NIMChatroomQueueElement>& keyValues) {
   // handle event
};
listener.onChatroomQueueBatchOffered = [](const std::vector<V2NIMChatroomQueueElement>& keyValues) {
   // handle event
};
chatroomQueueService.addQueueListener(listener);

```

Web/uni-app/小程序

调用 [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#on) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
chatroom.V2NIMChatroomQueueListener.on('onChatroomQueueBatchOffered', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.V2NIMChatroomQueueListener.on('onChatroomQueueBatchUpdated', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.V2NIMChatroomQueueListener.on('onChatroomQueueOffered', function (element: V2NIMChatroomQueueElement) {})
chatroom.V2NIMChatroomQueueListener.on('onChatroomQueuePartCleared', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.V2NIMChatroomQueueListener.on('onChatroomQueuePolled', function (element: V2NIMChatroomQueueElement) {})
chatroom.V2NIMChatroomQueueListener.on('onChatroomQueueDropped', function () {})

```

Node.js/Electron

调用 [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#on) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
chatroom.chatroomQueueListener.on('chatroomQueueBatchOffered', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.chatroomQueueListener.on('chatroomQueueBatchUpdated', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.chatroomQueueListener.on('chatroomQueueOffered', function (element: V2NIMChatroomQueueElement) {})
chatroom.chatroomQueueListener.on('chatroomQueuePartCleared', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.chatroomQueueListener.on('chatroomQueuePolled', function (element: V2NIMChatroomQueueElement) {})
chatroom.chatroomQueueListener.on('chatroomQueueDropped', function () {})

```

鸿蒙

调用 [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#on) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
chatroom.on('onChatroomQueueBatchOffered', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.on('onChatroomQueueBatchUpdated', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.on('onChatroomQueueOffered', function (element: V2NIMChatroomQueueElement) {})
chatroom.on('onChatroomQueuePartCleared', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.on('onChatroomQueuePolled', function (element: V2NIMChatroomQueueElement) {})
chatroom.on('onChatroomQueueDropped', function () {})

```

Flutter

调用 [`addQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#addQueueListener) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
import 'package:nim_core_v2/nim_core_v2.dart';

void addChatroomQueueListener(V2NIMChatroomQueueService queueService) async {
  NIMResult<void> result = await queueService.addQueueListener();
  if (result.isSuccess) {
    queueService.onChatroomQueueOffered.listen((element) {
      print('New queue element offered: ${element.key} - ${element.value}');
    });

    queueService.onChatroomQueuePolled.listen((element) {
      print('Queue element polled: ${element.key} - ${element.value}');
    });

    queueService.onChatroomQueueDropped.listen((_) {
      print('Queue dropped');
    });

    queueService.onChatroomQueuePartCleared.listen((elements) {
      print('Part of queue cleared: ${elements.length} elements');
    });

    queueService.onChatroomQueueBatchUpdated.listen((elements) {
      print('Queue elements batch updated: ${elements.length} elements');
    });

    queueService.onChatroomQueueBatchOffered.listen((elements) {
      print('Queue elements batch offered: ${elements.length} elements');
    });
  } else {
    print('Failed to add queue listener: ${result.code} - ${result.errorDetails}');
  }
}

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

调用 [`addQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#addQueueListener) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
V2NIMChatroomQueueListener queueListener = new V2NIMChatroomQueueListener() {
    @Override
    public void onChatroomQueueOffered(V2NIMChatroomQueueElement element) {
        //新增队列元素
    }

    @Override
    public void onChatroomQueuePolled(V2NIMChatroomQueueElement element) {
        //删除队列元素
    }

    @Override
    public void onChatroomQueueDropped() {
        //队列被清空
    }

    @Override
    public void onChatroomQueuePartCleared(List<V2NIMChatroomQueueElement> elements) {
        //部分队列元素被清除
    }

    @Override
    public void onChatroomQueueBatchUpdated(List<V2NIMChatroomQueueElement> elements) {
        //批量更新队列元素
    }

    @Override
    public void onChatroomQueueBatchOffered(List<V2NIMChatroomQueueElement> elements) {
        //批量新增队列元素
    }
};
//设置队列服务监听可以在进入聊天室之前就设置
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
chatroomQueueService.addQueueListener(queueListener);

```

调用 [`addQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#addQueueListener) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
@interface MyQueueListener : NSObject<V2NIMChatroomQueueListener>
@end
@implementation MyQueueListener

- (void)onChatroomQueueOffered:(V2NIMChatroomQueueElement *)element
{
    // 聊天室新增队列元素
}
- (void)onChatroomQueuePolled:(V2NIMChatroomQueueElement *)element
{
    // 聊天室移除队列元素
}
- (void)onChatroomQueueDropped
{
    // 聊天室清空队列元素
}
- (void)onChatroomQueuePartCleared:(NSArray<V2NIMChatroomQueueElement *> *)elements
{
    // 聊天室清理部分队列元素
}
- (void)onChatroomQueueBatchUpdated:(NSArray<V2NIMChatroomQueueElement *> *)elements
{
    // 聊天室批量更新队列元素
}
- (void)onChatroomQueueBatchOffered:(NSArray<V2NIMChatroomQueueElement *> *)elements
{
    // 聊天室批量添加队列元素
}
@end
MyQueueListener *queueListener = [[MyQueueListener alloc] init];
//设置队列服务监听可以在进入聊天室之前就设置
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
[chatroomQueueService addQueueListener:queueListener];

```

调用 [`addQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#addQueueListener) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
V2NIMChatroomQueueListener listener;
listener.onChatroomQueueOffered = [](const V2NIMChatroomQueueElement& element) {
   // handle event
};
listener.onChatroomQueuePolled = [](const V2NIMChatroomQueueElement& element) {
   // handle event
};
listener.onChatroomQueueDropped = []() {
   // handle event
};
listener.onChatroomQueuePartCleared = [](const std::vector<V2NIMChatroomQueueElement>& keyValues) {
   // handle event
};
listener.onChatroomQueueBatchUpdated = [](const std::vector<V2NIMChatroomQueueElement>& keyValues) {
   // handle event
};
listener.onChatroomQueueBatchOffered = [](const std::vector<V2NIMChatroomQueueElement>& keyValues) {
   // handle event
};
chatroomQueueService.addQueueListener(listener);

```

调用 [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#on) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
chatroom.V2NIMChatroomQueueListener.on('onChatroomQueueBatchOffered', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.V2NIMChatroomQueueListener.on('onChatroomQueueBatchUpdated', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.V2NIMChatroomQueueListener.on('onChatroomQueueOffered', function (element: V2NIMChatroomQueueElement) {})
chatroom.V2NIMChatroomQueueListener.on('onChatroomQueuePartCleared', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.V2NIMChatroomQueueListener.on('onChatroomQueuePolled', function (element: V2NIMChatroomQueueElement) {})
chatroom.V2NIMChatroomQueueListener.on('onChatroomQueueDropped', function () {})

```

调用 [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#on) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
chatroom.chatroomQueueListener.on('chatroomQueueBatchOffered', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.chatroomQueueListener.on('chatroomQueueBatchUpdated', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.chatroomQueueListener.on('chatroomQueueOffered', function (element: V2NIMChatroomQueueElement) {})
chatroom.chatroomQueueListener.on('chatroomQueuePartCleared', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.chatroomQueueListener.on('chatroomQueuePolled', function (element: V2NIMChatroomQueueElement) {})
chatroom.chatroomQueueListener.on('chatroomQueueDropped', function () {})

```

调用 [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#on) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
chatroom.on('onChatroomQueueBatchOffered', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.on('onChatroomQueueBatchUpdated', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.on('onChatroomQueueOffered', function (element: V2NIMChatroomQueueElement) {})
chatroom.on('onChatroomQueuePartCleared', function (elements: V2NIMChatroomQueueElement[]) {})
chatroom.on('onChatroomQueuePolled', function (element: V2NIMChatroomQueueElement) {})
chatroom.on('onChatroomQueueDropped', function () {})

```

调用 [`addQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#addQueueListener) 注册聊天室队列相关监听器，监听队列变更、清除等事件。

```
import 'package:nim_core_v2/nim_core_v2.dart';

void addChatroomQueueListener(V2NIMChatroomQueueService queueService) async {
  NIMResult<void> result = await queueService.addQueueListener();
  if (result.isSuccess) {
    queueService.onChatroomQueueOffered.listen((element) {
      print('New queue element offered: ${element.key} - ${element.value}');
    });

    queueService.onChatroomQueuePolled.listen((element) {
      print('Queue element polled: ${element.key} - ${element.value}');
    });

    queueService.onChatroomQueueDropped.listen((_) {
      print('Queue dropped');
    });

    queueService.onChatroomQueuePartCleared.listen((elements) {
      print('Part of queue cleared: ${elements.length} elements');
    });

    queueService.onChatroomQueueBatchUpdated.listen((elements) {
      print('Queue elements batch updated: ${elements.length} elements');
    });

    queueService.onChatroomQueueBatchOffered.listen((elements) {
      print('Queue elements batch offered: ${elements.length} elements');
    });
  } else {
    print('Failed to add queue listener: ${result.code} - ${result.errorDetails}');
  }
}

```

## Initialize the queue

调用 `queueInit` 方法初始化聊天室队列。

The sample code is as follows:

安卓

```
//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
chatroomQueueService.queueInit(100, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        //success
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //failed
    }
});

```

iOS

```
//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
[chatroomQueueService queueInit:100 success:^() {
    //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

macOS/Windows

```
chatroomQueueService.queueInit(100, []() {
    // success
}, [](const V2NIMError& error) {
    // handle error
});

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomQueueService.queueInit(100)

```

Node.js/Electron

```
await chatroomQueueService.queueInit(100)

```

鸿蒙

```
await chatroom.queueService.queueInit(100)

```

Flutter

```
import 'package:nim_core_v2/nim_core_v2.dart';

void initQueue(V2NIMChatroomQueueService queueService, int size) async {
  NIMResult<void> result = await queueService.queueInit(size);
  if (result.isSuccess) {
    print('Successfully initialized queue with size: $size');
  } else {
    print('Failed to initialize queue: ${result.code} - ${result.errorDetails}');
  }
}

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
chatroomQueueService.queueInit(100, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        //success
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //failed
    }
});

```

```
//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
[chatroomQueueService queueInit:100 success:^() {
    //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

```
chatroomQueueService.queueInit(100, []() {
    // success
}, [](const V2NIMError& error) {
    // handle error
});

```

```
await chatroom.V2NIMChatroomQueueService.queueInit(100)

```

```
await chatroomQueueService.queueInit(100)

```

```
await chatroom.queueService.queueInit(100)

```

```
import 'package:nim_core_v2/nim_core_v2.dart';

void initQueue(V2NIMChatroomQueueService queueService, int size) async {
  NIMResult<void> result = await queueService.queueInit(size);
  if (result.isSuccess) {
    print('Successfully initialized queue with size: $size');
  } else {
    print('Failed to initialize queue: ${result.code} - ${result.errorDetails}');
  }
}

```

## Add/update queue elements

调用 `queueOffer` 方法在队列中新增或更新元素。

新增或更新元素成功后，会触发 `onChatroomQueueOffered` 回调。

The sample code is as follows:

安卓

```
V2NIMChatroomClient v2NIMChatroomClient = V2NIMChatroomClient.newInstance();
V2NIMChatroomEnterParams enterParams = getEnterParams();
v2NIMChatroomClient.enter("100", enterParams, new V2NIMSuccessCallback<V2NIMChatroomEnterResult>() {
    @Override
    public void onSuccess(V2NIMChatroomEnterResult v2NIMChatroomEnterResult) {
        //进入聊天室成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //进入聊天室失败
    }
});

……

//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClientv2NIMChatroomClient.getChatroomQueueService();

V2NIMChatroomQueueOfferParams offerParams = new V2NIMChatroomQueueOfferParams("key1","value1");
//设置元素是否为瞬态的,true 表示元素是瞬态的,当前元素所属的成员退出或者掉线时会同步删除;false 表示元素是持久的,会被保留
offerParams.setTransient(true);
//设置元素属于的账号,默认为当前操作者,管理员操作可以指定元素属于的合法账号
offerParams.setElementOwnerAccountId("other account");
chatroomQueueService.queueOffer(offerParams, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        //success
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //failed
    }
});

```

iOS

```
V2NIMChatroomClient *client = [V2NIMChatroomClient newInstance];
V2NIMChatroomEnterParams *enterParams = [[V2NIMChatroomEnterParams alloc] init];
[client enter:@"100" enterParams:enterParams success:^(V2NIMChatroomEnterResult *result) {
    //进入聊天室成功
} failure:^(V2NIMError *error) {
    //进入聊天室失败
}];

……

//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];

V2NIMChatroomQueueOfferParams *offerParams = [[V2NIMChatroomQueueOfferParams alloc] init];
offerParams.elementKey = @"key";
offerParams.elementValue = @"value";
//设置元素是否为瞬态的。YES 表示元素是瞬态的,当前元素所属的成员退出或者掉线时会同步删除;NO 表示元素是持久的,会被保留
offerParams.transient = YES;
//设置元素属于的账号,默认为当前操作者,管理员操作可以指定元素属于的合法账号
offerParams.elementOwnerAccountId = @"other account";
[chatroomQueueService queueOffer:offerParams success:^() {
        //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

macOS/Windows

```
V2NIMChatroomQueueOfferParams params;
params.key = "key";
params.value = "value";
chatroomQueueService.queueOffer(params, []() {
   // success
}, [](const V2NIMError& error) {
   // handle error
});

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomQueueService.queueOffer(
  {
    "elementKey": "key5",
    "elementValue": "value5",
    "transient": false,
    "elementOwnerAccountId": "YOUR_ACCOUNT_ID"
  }
)

```

Node.js/Electron

```
await chatroomQueueService.queueOffer({
    elementKey: 'your element key',
    elementValue: 'your element value'
})

```

鸿蒙

```
await chatroom.queueService.queueOffer(
  {
    "elementKey": "key5",
    "elementValue": "value5",
    "transient": false,
    "elementOwnerAccountId": "YOUR_ACCOUNT_ID"
  }
)

```

Flutter

```
import 'package:nim_core_v2/nim_core_v2.dart';

void offerQueueElement(V2NIMChatroomQueueService queueService, String key, String value) async {
    final params = V2NIMChatroomQueueOfferParams(elementKey: key, elementValue: value);
    NIMResult<void> result = await queueService.queueOffer(params);
    if (result.isSuccess) {
      print('Successfully offered queue element: $key - $value');
    } else {
      print('Failed to offer queue element: ${result.code} - ${result.errorDetails}');
    }
  }

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2NIMChatroomClient = V2NIMChatroomClient.newInstance();
V2NIMChatroomEnterParams enterParams = getEnterParams();
v2NIMChatroomClient.enter("100", enterParams, new V2NIMSuccessCallback<V2NIMChatroomEnterResult>() {
    @Override
    public void onSuccess(V2NIMChatroomEnterResult v2NIMChatroomEnterResult) {
        //进入聊天室成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //进入聊天室失败
    }
});

……

//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClientv2NIMChatroomClient.getChatroomQueueService();

V2NIMChatroomQueueOfferParams offerParams = new V2NIMChatroomQueueOfferParams("key1","value1");
//设置元素是否为瞬态的,true 表示元素是瞬态的,当前元素所属的成员退出或者掉线时会同步删除;false 表示元素是持久的,会被保留
offerParams.setTransient(true);
//设置元素属于的账号,默认为当前操作者,管理员操作可以指定元素属于的合法账号
offerParams.setElementOwnerAccountId("other account");
chatroomQueueService.queueOffer(offerParams, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        //success
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //failed
    }
});

```

```
V2NIMChatroomClient *client = [V2NIMChatroomClient newInstance];
V2NIMChatroomEnterParams *enterParams = [[V2NIMChatroomEnterParams alloc] init];
[client enter:@"100" enterParams:enterParams success:^(V2NIMChatroomEnterResult *result) {
    //进入聊天室成功
} failure:^(V2NIMError *error) {
    //进入聊天室失败
}];

……

//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];

V2NIMChatroomQueueOfferParams *offerParams = [[V2NIMChatroomQueueOfferParams alloc] init];
offerParams.elementKey = @"key";
offerParams.elementValue = @"value";
//设置元素是否为瞬态的。YES 表示元素是瞬态的,当前元素所属的成员退出或者掉线时会同步删除;NO 表示元素是持久的,会被保留
offerParams.transient = YES;
//设置元素属于的账号,默认为当前操作者,管理员操作可以指定元素属于的合法账号
offerParams.elementOwnerAccountId = @"other account";
[chatroomQueueService queueOffer:offerParams success:^() {
        //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

```
V2NIMChatroomQueueOfferParams params;
params.key = "key";
params.value = "value";
chatroomQueueService.queueOffer(params, []() {
   // success
}, [](const V2NIMError& error) {
   // handle error
});

```

```
await chatroom.V2NIMChatroomQueueService.queueOffer(
  {
    "elementKey": "key5",
    "elementValue": "value5",
    "transient": false,
    "elementOwnerAccountId": "YOUR_ACCOUNT_ID"
  }
)

```

```
await chatroomQueueService.queueOffer({
    elementKey: 'your element key',
    elementValue: 'your element value'
})

```

```
await chatroom.queueService.queueOffer(
  {
    "elementKey": "key5",
    "elementValue": "value5",
    "transient": false,
    "elementOwnerAccountId": "YOUR_ACCOUNT_ID"
  }
)

```

```
import 'package:nim_core_v2/nim_core_v2.dart';

void offerQueueElement(V2NIMChatroomQueueService queueService, String key, String value) async {
    final params = V2NIMChatroomQueueOfferParams(elementKey: key, elementValue: value);
    NIMResult<void> result = await queueService.queueOffer(params);
    if (result.isSuccess) {
      print('Successfully offered queue element: $key - $value');
    } else {
      print('Failed to offer queue element: ${result.code} - ${result.errorDetails}');
    }
  }

```

## Batch update queue elements

调用 `queueBatchUpdate` 方法批量更新队列元素。

批量更新成功后，会触发 `onChatroomQueueBatchUpdated` 回调。

The sample code is as follows:

安卓

```
//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
//待更新队列元素列表
List<V2NIMChatroomQueueElement> elements = new ArrayList<>();
elements.add(new V2NIMChatroomQueueElement("key1", "value1 new"));
elements.add(new V2NIMChatroomQueueElement("key2", "value2 new"));
//是否发送广播通知
boolean notificationEnabled = true;
//本次操作生成的通知中的扩展字段
String notificationExtension = "notificationExtension";
chatroomQueueService.queueBatchUpdate(elements, notificationEnabled, notificationExtension, new V2NIMSuccessCallback<List<String>>() {
    @Override
    public void onSuccess(List<String> result) {
        //success，result is non-existent element key list

    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //failed
    }
});

```

iOS

```
//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
//待更新队列元素列表
NSMutableArray<V2NIMChatroomQueueElement *> *elements = [NSMutableArray array];
V2NIMChatroomQueueElement* element;
element = [[V2NIMChatroomQueueElement alloc] init];
element.key = @"key1";
element.value = @"value1 new";
[elements addObject:element];

element = [[V2NIMChatroomQueueElement alloc] init];
element.key = @"key1";
element.value = @"value1 new";
[elements addObject:element];

//是否发送广播通知
BOOL notificationEnabled = YES;
//本次操作生成的通知中的扩展字段
NSString *notificationExtension = @"notificationExtension";
[chatroomQueueService queueBatchUpdate:elements notificationEnabled:notificationEnabled notificationExtension:notificationExtension success:^(NSArray<NSString *> *result) {
    //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

macOS/Windows

```
V2NIMChatroomQueueElement element;
element.key = "key";
element.value = "value";
nstd::vector<V2NIMChatroomQueueElement> elements;
elements.push_back(element);
chatroomQueueService.queueBatchUpdate(elements, true, "notificationExtension", [](const nstd::vector<nstd::string>& failedKeys) {
    // success
}, [](const V2NIMError& error) {
    // handle error
});

```

Web/uni-app/小程序

```
 const invalidKeys = await chatroom.V2NIMChatroomQueueService.queueBatchUpdate([
   {
     "key": "key1",
     "value": "valueA"
   },
   {
     "key": "key2",
     "value": "valueB"
   }
 ], true, 'An extension')

 console.log(invalidKeys) // ["key2"], because "key2" is not exist

```

Node.js/Electron

```
const elementKeys = await chatroomQueueService.queueBatchUpdate([{
    key: 'your element key',
    value: 'your element value'
}], true, 'your notification extension')

```

鸿蒙

```
const invalidKeys = await chatroom.queueService.queueBatchUpdate([
{
    "key": "key1",
    "value": "valueA"
},
{
    "key": "key2",
    "value": "valueB"
}
], true, 'An extension')

console.log(invalidKeys) // ["key2"], because "key2" is not exist

```

Flutter

```
import 'package:nim_core_v2/nim_core_v2.dart';

void batchUpdateQueueElements(V2NIMChatroomQueueService queueService, List<V2NIMChatroomQueueElement> elements) async {
  NIMResult<List<String>?> result = await queueService.queueBatchUpdate(elements);
  if (result.isSuccess) {
    print('Successfully batch updated queue elements');
    if (result.data != null) {
      print('Non-existent elements: ${result.data}');
    }
  } else {
    print('Failed to batch update queue elements: ${result.code} - ${result.errorDetails}');
  }
}

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
//待更新队列元素列表
List<V2NIMChatroomQueueElement> elements = new ArrayList<>();
elements.add(new V2NIMChatroomQueueElement("key1", "value1 new"));
elements.add(new V2NIMChatroomQueueElement("key2", "value2 new"));
//是否发送广播通知
boolean notificationEnabled = true;
//本次操作生成的通知中的扩展字段
String notificationExtension = "notificationExtension";
chatroomQueueService.queueBatchUpdate(elements, notificationEnabled, notificationExtension, new V2NIMSuccessCallback<List<String>>() {
    @Override
    public void onSuccess(List<String> result) {
        //success，result is non-existent element key list

    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //failed
    }
});

```

```
//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
//待更新队列元素列表
NSMutableArray<V2NIMChatroomQueueElement *> *elements = [NSMutableArray array];
V2NIMChatroomQueueElement* element;
element = [[V2NIMChatroomQueueElement alloc] init];
element.key = @"key1";
element.value = @"value1 new";
[elements addObject:element];

element = [[V2NIMChatroomQueueElement alloc] init];
element.key = @"key1";
element.value = @"value1 new";
[elements addObject:element];

//是否发送广播通知
BOOL notificationEnabled = YES;
//本次操作生成的通知中的扩展字段
NSString *notificationExtension = @"notificationExtension";
[chatroomQueueService queueBatchUpdate:elements notificationEnabled:notificationEnabled notificationExtension:notificationExtension success:^(NSArray<NSString *> *result) {
    //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

```
V2NIMChatroomQueueElement element;
element.key = "key";
element.value = "value";
nstd::vector<V2NIMChatroomQueueElement> elements;
elements.push_back(element);
chatroomQueueService.queueBatchUpdate(elements, true, "notificationExtension", [](const nstd::vector<nstd::string>& failedKeys) {
    // success
}, [](const V2NIMError& error) {
    // handle error
});

```

```
 const invalidKeys = await chatroom.V2NIMChatroomQueueService.queueBatchUpdate([
   {
     "key": "key1",
     "value": "valueA"
   },
   {
     "key": "key2",
     "value": "valueB"
   }
 ], true, 'An extension')

 console.log(invalidKeys) // ["key2"], because "key2" is not exist

```

```
const elementKeys = await chatroomQueueService.queueBatchUpdate([{
    key: 'your element key',
    value: 'your element value'
}], true, 'your notification extension')

```

```
const invalidKeys = await chatroom.queueService.queueBatchUpdate([
{
    "key": "key1",
    "value": "valueA"
},
{
    "key": "key2",
    "value": "valueB"
}
], true, 'An extension')

console.log(invalidKeys) // ["key2"], because "key2" is not exist

```

```
import 'package:nim_core_v2/nim_core_v2.dart';

void batchUpdateQueueElements(V2NIMChatroomQueueService queueService, List<V2NIMChatroomQueueElement> elements) async {
  NIMResult<List<String>?> result = await queueService.queueBatchUpdate(elements);
  if (result.isSuccess) {
    print('Successfully batch updated queue elements');
    if (result.data != null) {
      print('Non-existent elements: ${result.data}');
    }
  } else {
    print('Failed to batch update queue elements: ${result.code} - ${result.errorDetails}');
  }
}

```

## Take out the specified queue element

调用 `queuePoll` 方法取出队列中的指定元素。若未指定元素，则默认取出队列的头元素。

取出成功后，会触发 `onChatroomQueuePolled` 回调。

The sample code is as follows:

安卓

```
//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
chatroomQueueService.queuePoll("key1", new V2NIMSuccessCallback<V2NIMChatroomQueueElement>() {
    @Override
    public void onSuccess(V2NIMChatroomQueueElement element) {
       //success
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //failed
    }
});

```

iOS

```
//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
[chatroomQueueService queuePoll:@"key1" success:^(V2NIMChatroomQueueElement *result) {
    //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

macOS/Windows

```
chatroomQueueService.queuePoll("key", [](const V2NIMChatroomQueueElement& element) {
    // success
}, [](const V2NIMError& error) {
    // handle error
});

```

Web/uni-app/小程序

```
const element = await chatroom.V2NIMChatroomQueueService.queuePoll("key1")

```

Node.js/Electron

```
const element = await chatroomQueueService.queuePoll('your element key')

```

鸿蒙

```
const element = await chatroom.queueService.queuePoll("key1")

```

Flutter

```
import 'package:nim_core_v2/nim_core_v2.dart';

void pollQueueElement(V2NIMChatroomQueueService queueService, String? elementKey) async {
    NIMResult<V2NIMChatroomQueueElement> result = await queueService.queuePoll(elementKey ?? '');
    if (result.isSuccess && result.data != null) {
      print('Successfully polled queue element: ${result.data!.key} - ${result.data!.value}');
    } else {
      print('Failed to poll queue element: ${result.code} - ${result.errorDetails}');
    }
  }

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
chatroomQueueService.queuePoll("key1", new V2NIMSuccessCallback<V2NIMChatroomQueueElement>() {
    @Override
    public void onSuccess(V2NIMChatroomQueueElement element) {
       //success
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //failed
    }
});

```

```
//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
[chatroomQueueService queuePoll:@"key1" success:^(V2NIMChatroomQueueElement *result) {
    //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

```
chatroomQueueService.queuePoll("key", [](const V2NIMChatroomQueueElement& element) {
    // success
}, [](const V2NIMError& error) {
    // handle error
});

```

```
const element = await chatroom.V2NIMChatroomQueueService.queuePoll("key1")

```

```
const element = await chatroomQueueService.queuePoll('your element key')

```

```
const element = await chatroom.queueService.queuePoll("key1")

```

```
import 'package:nim_core_v2/nim_core_v2.dart';

void pollQueueElement(V2NIMChatroomQueueService queueService, String? elementKey) async {
    NIMResult<V2NIMChatroomQueueElement> result = await queueService.queuePoll(elementKey ?? '');
    if (result.isSuccess && result.data != null) {
      print('Successfully polled queue element: ${result.data!.key} - ${result.data!.value}');
    } else {
      print('Failed to poll queue element: ${result.code} - ${result.errorDetails}');
    }
  }

```

## Query the queue header element

调用 `queuePeek` 方法查看队列的头元素。

The sample code is as follows:

安卓

```
//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
chatroomQueueService.queuePeek(new V2NIMSuccessCallback<V2NIMChatroomQueueElement>() {
    @Override
    public void onSuccess(V2NIMChatroomQueueElement element) {
        //success
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
      //failed
    }
});

```

iOS

```
//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
[chatroomQueueService queuePeek:^(V2NIMChatroomQueueElement *result) {
    //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

macOS/Windows

```
chatroomQueueService.queuePeek([](const V2NIMChatroomQueueElement& element) {
    // success
}, [](const V2NIMError& error) {
    // handle error
});

```

Web/uni-app/小程序

```
const element = await chatroom.V2NIMChatroomQueueService.queuePeek()

```

Node.js/Electron

```
const element = await chatroomQueueService.queuePeek()

```

鸿蒙

```
const element = await chatroom.queueService.queuePeek()

```

Flutter

```
import 'package:nim_core_v2/nim_core_v2.dart';

void peekQueueElement(V2NIMChatroomQueueService queueService) async {
  NIMResult<V2NIMChatroomQueueElement> result = await queueService.queuePeek();
  if (result.isSuccess && result.data != null) {
    print('Queue head element: ${result.data!.key} - ${result.data!.value}');
  } else {
    print('Failed to peek queue element: ${result.code} - ${result.errorDetails}');
  }
}

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
chatroomQueueService.queuePeek(new V2NIMSuccessCallback<V2NIMChatroomQueueElement>() {
    @Override
    public void onSuccess(V2NIMChatroomQueueElement element) {
        //success
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
      //failed
    }
});

```

```
//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
[chatroomQueueService queuePeek:^(V2NIMChatroomQueueElement *result) {
    //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

```
chatroomQueueService.queuePeek([](const V2NIMChatroomQueueElement& element) {
    // success
}, [](const V2NIMError& error) {
    // handle error
});

```

```
const element = await chatroom.V2NIMChatroomQueueService.queuePeek()

```

```
const element = await chatroomQueueService.queuePeek()

```

```
const element = await chatroom.queueService.queuePeek()

```

```
import 'package:nim_core_v2/nim_core_v2.dart';

void peekQueueElement(V2NIMChatroomQueueService queueService) async {
  NIMResult<V2NIMChatroomQueueElement> result = await queueService.queuePeek();
  if (result.isSuccess && result.data != null) {
    print('Queue head element: ${result.data!.key} - ${result.data!.value}');
  } else {
    print('Failed to peek queue element: ${result.code} - ${result.errorDetails}');
  }
}

```

## Query all elements of the queue

调用 `queueList` 方法排序列出所有队列元素。

The sample code is as follows:

安卓

```
//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
chatroomQueueService.queueList(new V2NIMSuccessCallback<List<V2NIMChatroomQueueElement>>() {
    @Override
    public void onSuccess(List<V2NIMChatroomQueueElement> v2NIMChatroomQueueElements) {
        //success
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //failed
    }
});

```

iOS

```
//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
[chatroomQueueService queueList:^(NSArray<V2NIMChatroomQueueElement *> *result) {
    //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

macOS/Windows

```
chatroomQueueService.queueList([](const nstd::vector<V2NIMChatroomQueueElement>& elements) {
    // success
}, [](const V2NIMError& error) {
    // handle error
});

```

Web/uni-app/小程序

```
const elements = await chatroom.V2NIMChatroomQueueService.queueList()

```

Node.js/Electron

```
const elements = await chatroomQueueService.queueList()
console.log(elements)

```

鸿蒙

```
const elements = await chatroom.queueService.queueList()

```

Flutter

```
import 'package:nim_core_v2/nim_core_v2.dart';

void listQueueElements(V2NIMChatroomQueueService queueService) async {
  NIMResult<List<V2NIMChatroomQueueElement>> result = await queueService.queueList();
  if (result.isSuccess && result.data != null) {
    print('Queue elements:');
    for (var element in result.data!) {
      print('${element.key} - ${element.value}');
    }
  } else {
    print('Failed to list queue elements: ${result.code} - ${result.errorDetails}');
  }
}

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
chatroomQueueService.queueList(new V2NIMSuccessCallback<List<V2NIMChatroomQueueElement>>() {
    @Override
    public void onSuccess(List<V2NIMChatroomQueueElement> v2NIMChatroomQueueElements) {
        //success
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //failed
    }
});

```

```
//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
[chatroomQueueService queueList:^(NSArray<V2NIMChatroomQueueElement *> *result) {
    //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

```
chatroomQueueService.queueList([](const nstd::vector<V2NIMChatroomQueueElement>& elements) {
    // success
}, [](const V2NIMError& error) {
    // handle error
});

```

```
const elements = await chatroom.V2NIMChatroomQueueService.queueList()

```

```
const elements = await chatroomQueueService.queueList()
console.log(elements)

```

```
const elements = await chatroom.queueService.queueList()

```

```
import 'package:nim_core_v2/nim_core_v2.dart';

void listQueueElements(V2NIMChatroomQueueService queueService) async {
  NIMResult<List<V2NIMChatroomQueueElement>> result = await queueService.queueList();
  if (result.isSuccess && result.data != null) {
    print('Queue elements:');
    for (var element in result.data!) {
      print('${element.key} - ${element.value}');
    }
  } else {
    print('Failed to list queue elements: ${result.code} - ${result.errorDetails}');
  }
}

```

## Empty the chat room queue

调用 `queueDrop` 方法清空聊天室队列。

清空成功后，会触发 `onChatroomQueueDropped` 回调。

The sample code is as follows:

安卓

```
//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
chatroomQueueService.queueDrop(new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        //success
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //failed
    }
});

```

iOS

```
//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
[chatroomQueueService queueDrop:^() {
    //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

macOS/Windows

```
chatroomQueueService.queueDrop([]() {
   // success
}, [](const V2NIMError& error) {
   // handle error
});

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomQueueService.queueDrop()

```

Node.js/Electron

```
await chatroomQueueService.queueDrop()

```

鸿蒙

```
await chatroom.queueService.queueDrop()

```

Flutter

```
import 'package:nim_core_v2/nim_core_v2.dart';

void dropQueue(V2NIMChatroomQueueService queueService) async {
  NIMResult<void> result = await queueService.queueDrop();
  if (result.isSuccess) {
    print('Successfully dropped queue');
  } else {
    print('Failed to drop queue: ${result.code} - ${result.errorDetails}');
  }
}

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
//进入聊天室成功后才能使用队列服务
V2NIMChatroomQueueService chatroomQueueService = v2NIMChatroomClient.getChatroomQueueService();
chatroomQueueService.queueDrop(new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        //success
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //failed
    }
});

```

```
//进入聊天室成功后才能使用队列服务
id<V2NIMChatroomQueueService> chatroomQueueService = [v2NIMChatroomClient getChatroomQueueService];
[chatroomQueueService queueDrop:^() {
    //success
} failure:^(V2NIMError *error) {
    //failed
}];

```

```
chatroomQueueService.queueDrop([]() {
   // success
}, [](const V2NIMError& error) {
   // handle error
});

```

```
await chatroom.V2NIMChatroomQueueService.queueDrop()

```

```
await chatroomQueueService.queueDrop()

```

```
await chatroom.queueService.queueDrop()

```

```
import 'package:nim_core_v2/nim_core_v2.dart';

void dropQueue(V2NIMChatroomQueueService queueService) async {
  NIMResult<void> result = await queueService.queueDrop();
  if (result.isSuccess) {
    print('Successfully dropped queue');
  } else {
    print('Failed to drop queue: ${result.code} - ${result.errorDetails}');
  }
}

```

## Related interfaces

安卓/iOS/macOS/Windows

| API                                                                                                                                                   | 说明                           |
| ----------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------ |
| [`V2NIMChatroomClient.getChatroomQueueService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomQueueService) | 获取聊天室队列服务类           |
| [`addQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#addQueueListener)                                   | 注册聊天室队列监听器           |
| [`removeQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#removeQueueListener)                             | 取消注册聊天室队列监听器       |
| [`queueInit`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueInit)                                                 | 初始化聊天室队列               |
| [`queueOffer`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueOffer)                                               | 在队列中新增或更新元素         |
| [`queueBatchUpdate`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueBatchUpdate)                                   | 批量更新队列元素               |
| [`queuePoll`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queuePoll)                                                 | 在队列中取出头元素或指定的元素 |
| [`queuePeek`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queuePeek)                                                 | 查询队列的头元素               |
| [`queueList`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueList)                                                 | 排序列出所有队列元素           |
| [`queueDrop`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueDrop)                                                 | 清空队列                       |

Web/uni-app/小程序/Node.js/Electron/鸿蒙

| API                                                                                                                                                   | 说明                           |
| ----------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------ |
| [`V2NIMChatroomClient.getChatroomQueueService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomQueueService) | 获取聊天室队列服务类           |
| [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#on)                                                  | 注册聊天室队列监听器           |
| [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#off)                                                | 取消注册聊天室队列监听器       |
| [`queueInit`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueInit)                                                 | 初始化聊天室队列               |
| [`queueOffer`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueOffer)                                               | 在队列中新增或更新元素         |
| [`queueBatchUpdate`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueBatchUpdate)                                   | 批量更新队列元素               |
| [`queuePoll`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queuePoll)                                                 | 在队列中取出头元素或指定的元素 |
| [`queuePeek`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queuePeek)                                                 | 查询队列的头元素               |
| [`queueList`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueList)                                                 | 排序列出所有队列元素           |
| [`queueDrop`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueDrop)                                                 | 清空队列                       |

Flutter

| API                                                                                                                                                   | 说明                           |
| ----------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------ |
| [`V2NIMChatroomClient.getChatroomQueueService`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomQueueService) | 获取聊天室队列服务类           |
| [`addQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#addQueueListener)                                   | 注册聊天室队列监听器           |
| [`removeQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#removeQueueListener)                             | 取消注册聊天室队列监听器       |
| [`queueInit`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queueInit)                                                 | 初始化聊天室队列               |
| [`queueOffer`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queueOffer)                                               | 在队列中新增或更新元素         |
| [`queueBatchUpdate`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queueBatchUpdate)                                   | 批量更新队列元素               |
| [`queuePoll`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queuePoll)                                                 | 在队列中取出头元素或指定的元素 |
| [`queuePeek`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queuePeek)                                                 | 查询队列的头元素               |
| [`queueList`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queueList)                                                 | 排序列出所有队列元素           |
| [`queueDrop`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queueDrop)                                                 | 清空队列                       |

Android/iOS/macOS/WindowsWeb/uni-app/applet/Node.js/Electron/HongmengFlutter

| API                                                                                                                                                   | Explain                                                         |
| ----------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------- |
| [`V2NIMChatroomClient.getChatroomQueueService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomQueueService) | Get the chat room queue service class                           |
| [`addQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#addQueueListener)                                   | Register the chat room queue listener                           |
| [`removeQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#removeQueueListener)                             | Unregister the chat room queue listener                         |
| [`queueInit`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueInit)                                                 | Initialize the chat room queue                                  |
| [`queueOffer`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueOffer)                                               | Add or update elements in the queue                             |
| [`queueBatchUpdate`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueBatchUpdate)                                   | Batch update queue elements                                     |
| [`queuePoll`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queuePoll)                                                 | Remove the head element or the specified element from the queue |
| [`queuePeek`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queuePeek)                                                 | The head element of the query queue                             |
| [`queueList`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueList)                                                 | Sort and list all queue elements                                |
| [`queueDrop`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueDrop)                                                 | Clear the queue                                                 |

| API                                                                                                                                                   | 说明                           |
| ----------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------ |
| [`V2NIMChatroomClient.getChatroomQueueService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomQueueService) | 获取聊天室队列服务类           |
| [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#on)                                                  | 注册聊天室队列监听器           |
| [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#off)                                                | 取消注册聊天室队列监听器       |
| [`queueInit`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueInit)                                                 | 初始化聊天室队列               |
| [`queueOffer`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueOffer)                                               | 在队列中新增或更新元素         |
| [`queueBatchUpdate`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueBatchUpdate)                                   | 批量更新队列元素               |
| [`queuePoll`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queuePoll)                                                 | 在队列中取出头元素或指定的元素 |
| [`queuePeek`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queuePeek)                                                 | 查询队列的头元素               |
| [`queueList`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueList)                                                 | 排序列出所有队列元素           |
| [`queueDrop`](https://doc.yunxin.163.com/messaging2/client-apis/Dk5MjczOTQ?platform=client#queueDrop)                                                 | 清空队列                       |

| API                                                                                                                                                   | 说明                           |
| ----------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------ |
| [`V2NIMChatroomClient.getChatroomQueueService`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomQueueService) | 获取聊天室队列服务类           |
| [`addQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#addQueueListener)                                   | 注册聊天室队列监听器           |
| [`removeQueueListener`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#removeQueueListener)                             | 取消注册聊天室队列监听器       |
| [`queueInit`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queueInit)                                                 | 初始化聊天室队列               |
| [`queueOffer`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queueOffer)                                               | 在队列中新增或更新元素         |
| [`queueBatchUpdate`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queueBatchUpdate)                                   | 批量更新队列元素               |
| [`queuePoll`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queuePoll)                                                 | 在队列中取出头元素或指定的元素 |
| [`queuePeek`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queuePeek)                                                 | 查询队列的头元素               |
| [`queueList`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queueList)                                                 | 排序列出所有队列元素           |
| [`queueDrop`](https://doc.yunxin.163.com/messaging2/client-apis/jU2NTkxNDE?platform=client#queueDrop)                                                 | 清空队列                       |
