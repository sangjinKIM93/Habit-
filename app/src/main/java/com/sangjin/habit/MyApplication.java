package com.sangjin.habit;

import android.app.Application;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(1)
//                .migration(new RealmMigration() {
//                    @Override
//                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
//
//                        RealmSchema schema = realm.getSchema();
//
//                        if(oldVersion == 1){
//                            schema.create("ChatData")
//                                    .addField("name", String.class, FieldAttribute.REQUIRED)
//                                    .addField("content", String.class, FieldAttribute.REQUIRED)
//                                    .addField("imagePath", String.class)
//                                    .addField("viewtype", int.class)
//                                    .addField("togetherIdx", int.class);
//                        }
//                    }
//                })
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
