package com.netease.im.session.extension;

import com.alibaba.fastjson.JSONObject;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.netease.im.MessageConstant;

public class WarningBlockAttachment extends CustomAttachment {
    private String blockType;
    private Integer dayBlocks;
    private String dayExpiredBlock;

    public WarningBlockAttachment() {
        super(CustomAttachmentType.WARNING_BLOCK);
    }

    public WritableMap getWritableMap() {
        return toReactNative();
    }

    @Override
    protected void parseData(JSONObject data) {
        blockType = data.getString(MessageConstant.WarningBlock.BLOCK_TYPE);
        if (blockType.equals("TEMPORARY_LOCK")) {
            dayBlocks = Integer.parseInt(data.getString(MessageConstant.WarningBlock.DAYS_BLOCK));
            dayExpiredBlock = data.getString(MessageConstant.WarningBlock.DAY_EXPIRED_BLOCK);
        }
    }

    @Override
    protected JSONObject packData() {
        JSONObject object = new JSONObject();
        object.put(MessageConstant.WarningBlock.BLOCK_TYPE, blockType);
        if (blockType.equals("TEMPORARY_LOCK")) {
            object.put(MessageConstant.WarningBlock.DAYS_BLOCK, dayBlocks);
            object.put(MessageConstant.WarningBlock.DAY_EXPIRED_BLOCK, dayExpiredBlock);
        }

        return object;
    }

    @Override
    protected WritableMap toReactNative() {
        WritableMap writableMap = Arguments.createMap();
        writableMap.putString(MessageConstant.WarningBlock.BLOCK_TYPE, blockType);
        if (blockType.equals("TEMPORARY_LOCK")) {
            writableMap.putInt(MessageConstant.WarningBlock.DAYS_BLOCK, dayBlocks);
            writableMap.putString(MessageConstant.WarningBlock.DAY_EXPIRED_BLOCK, dayExpiredBlock);
        }
        return writableMap;
    }
}
