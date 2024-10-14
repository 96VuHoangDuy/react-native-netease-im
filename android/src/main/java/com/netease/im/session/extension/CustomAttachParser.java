package com.netease.im.session.extension;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.im.MessageConstant;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser;

/**
 * Created by zhoujianghua on 2015/4/9.
 */
public class CustomAttachParser implements MsgAttachmentParser {

    private static final String KEY_TYPE = "msgtype";
    private static final String KEY_DATA = "data";

    private CustomAttachment getAttachmentWithData(JSONObject data) {
        Log.d("TEST DATA", data.toJSONString());
        String dataLoginType = data.getString(MessageConstant.WarningLogin.WARINING_TYPE);
        if (dataLoginType != null) {
            return new WarningLoginAttachment();
        }

        String dataBlockType = data.getString(MessageConstant.WarningBlock.BLOCK_TYPE);
        if (dataBlockType != null && (dataBlockType.equals("PERMANENT_LOCK") || dataBlockType.equals("TEMPORARY_LOCK"))) {
            return new WarningBlockAttachment();
        }

        if (data.get("data") != null && data.get("code") != null) {
            return new CustomMessageChatBotAttachment();
        }

        return null;
    }

    @Override
    public MsgAttachment parse(String json) {
        CustomAttachment attachment = null;
        try {
            JSONObject object = JSON.parseObject(json);
            String type = object.getString(KEY_TYPE);
            JSONObject data = object.getJSONObject(KEY_DATA);

            CustomAttachment attachmentData = getAttachmentWithData(data);
            if (attachmentData != null) {
                attachmentData.fromJson(data);

                return attachmentData;
            }

            switch (type) {
                case CustomAttachmentType.ForwardMultipleText:
                    attachment = new ForwardMultipleTextAttachment();
                    break;
                case CustomAttachmentType.RedPacket:
                    attachment = new RedPacketAttachement();
                    break;
                case CustomAttachmentType.BankTransfer:
                    attachment = new BankTransferAttachment();
                    break;
                case CustomAttachmentType.BankTransferSystem:
                    attachment = new BankTransferSystemAttachment();
                    break;
                case CustomAttachmentType.RedPacketOpen:
                    attachment = new RedPacketOpenAttachement();
                    break;
                case CustomAttachmentType.LinkUrl:
                    attachment = new LinkUrlAttachment();
                    break;
                case CustomAttachmentType.AccountNotice:
                    attachment = new AccountNoticeAttachment();
                    break;
                case CustomAttachmentType.Card:
                    attachment = new CardAttachment();
                    break;
                default:
                    attachment = new DefaultCustomAttachment(type);
                    break;
            }

            attachment.fromJson(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attachment;
    }

    public static String packData(String type, JSONObject data) {
        Integer code = (Integer) data.get("code");
        if (code != null && code == 18939912) {
            return data.toJSONString();
        }

        JSONObject object = new JSONObject();
        object.put(KEY_TYPE, type);
        if (data != null) {
            object.put(KEY_DATA, data);
        }

        return object.toJSONString();
    }
}
