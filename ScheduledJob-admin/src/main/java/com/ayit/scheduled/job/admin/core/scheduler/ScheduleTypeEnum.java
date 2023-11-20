package com.ayit.scheduled.job.admin.core.scheduler;


import com.ayit.scheduled.job.admin.core.util.I18nUtil;

public enum ScheduleTypeEnum {

    //不使用任何类型
    NONE(I18nUtil.getString("schedule_type_none")),

    //一般都是用cron表达式
    CRON(I18nUtil.getString("schedule_type_cron")),

    //按照固定频率
    FIX_RATE(I18nUtil.getString("schedule_type_fix_rate"));


    private String title;

    ScheduleTypeEnum(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static ScheduleTypeEnum match(String name, ScheduleTypeEnum defaultItem){
        for (ScheduleTypeEnum item: ScheduleTypeEnum.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return defaultItem;
    }

}
