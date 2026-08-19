Error code|Hex|Description
-:|-:|:-
1||general error, It's not exclusive, indicates an error only.
---Account correlation---||
1000001||App's error：not set the information of the CommsEase.
1000002||The designated ZYZJ account inexistence.
1000003||The designated accid inexistence or not find corresponding tothe information of the ZYZJ account.
1010001||Account registration failure：Failed to create account information.
1010002||Account registration failure：Failed to create the accid information of the CommsEase.
1020001||login failure：Unable to create login token
1030001||Failed to save the account data
---Friend's relation---||
2010001||The specified account cannot be added as friend's relation
2010002||Not friend's relation,Unable to perform related operations
2020001||Failed to save the friend notes
---Group chat-------||
3010001||The specified group chat does not exist
---Customer service-------||
4000001||Not set the account information of the customer service robot
---Chatroom-----||
5000001||Failed to create a chat room, Like the same name or something.
5000002||Failed to create a chat room, Unable to create chat rooms on the CommsEase's server.
5000003||Failed to close the chat room
5000004||Failed to mute the chat room
5000005||Failed to broatcast all chat room
---system-------||
4294967294|0xFFFFFFFE|Sign error
4294967295|0xFFFFFFFF|Illegal request: The request does not meet the requirements of the communication protocol.
