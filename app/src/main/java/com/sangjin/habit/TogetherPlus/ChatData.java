package com.sangjin.habit.TogetherPlus;

import io.realm.RealmObject;

public class ChatData extends RealmObject {

    String uid;
    String name;
    String imagePath;
    String content;
    int viewType;
    int togetherIdx;
    String created;

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public int getTogetherIdx() {
        return togetherIdx;
    }

    public void setTogetherIdx(int togetherIdx) {
        this.togetherIdx = togetherIdx;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }


    @Override
    public String toString() {
        return "ChatData{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", content='" + content + '\'' +
                ", viewType=" + viewType +
                ", togetherIdx=" + togetherIdx +
                '}';
    }
}
