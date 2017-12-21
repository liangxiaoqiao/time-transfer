package com.liangxiaoqiao.plugin.time.main;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by liangchao on 17-12-21.
 */
@State(name = "TimeTransferConfig", storages = {@Storage(
        id = "other",
        file = "$APP_CONFIG$/timeTransfer.xml")
})
public class TimeTransferService implements PersistentStateComponent<TimeTransferService> {

    /**
     * @Transient 用这个注解告诉IDEA不要序列化这个属性
     * In order to exclude a public field or bean property from serialization,
     * you can annotate the field or getter with the @com.intellij.util.xmlb.annotations.Transient annotation.
     */
    public String usedPattern;
    public String defaultPattern = "yyyy-MM-dd HH:mm:ss";
    public String leftString;
    public String rightString;
    public List<String> patterns;

    public TimeTransferService() {
        initPattern();
    }

    private void initPattern(){
        patterns = new LinkedList<>();
        List<String> temp = new LinkedList<>();
        temp.add("yyyy-MM-dd");
        temp.add("yyyy/MM/dd");
        temp.add("yyyy MM dd");
        temp.add("dd/MM/yyyy");
        temp.add("dd-MM-yyyy");
        temp.add("dd MM yyyy");

        temp.add("yyyy-MMM-dd");
        temp.add("yyyy/MMM/dd");
        temp.add("yyyy MMM dd");
        temp.add("dd/MMM/yyyy");
        temp.add("dd-MMM-yyyy");
        temp.add("dd MMM yyyy");

        temp.add("yy-MMM-dd");
        temp.add("yy/MMM/dd");
        temp.add("yy MMM dd");
        temp.add("dd/MMM/yy");
        temp.add("dd-MMM-yy");
        temp.add("dd MMM yy");
        temp.add("yy-MM-dd");
        temp.add("yy/MM/dd");
        temp.add("yy MM dd");
        temp.add("dd/MM/yy");
        temp.add("dd-MM-yy");
        temp.add("dd MM yy");

        temp.stream().forEach(t ->{
            patterns.add(t+" HH:mm:ss");
            patterns.add(t+" hh:mm:ss");
            patterns.add(t);
        });
    }

    public void addFirst(String pattern) {
        this.patterns.add(0, pattern);
    }

    public void removeFirst() {
        this.patterns.remove(0);
    }

    @Nullable
    @Override
    public TimeTransferService getState() {
        return this;
    }

    @Override
    public void loadState(TimeTransferService timeTransferService) {
        XmlSerializerUtil.copyBean(timeTransferService, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeTransferService that = (TimeTransferService) o;

        if (usedPattern != null ? !usedPattern.equals(that.usedPattern) : that.usedPattern != null)
            return false;
        if (defaultPattern != null ? !defaultPattern.equals(that.defaultPattern) : that.defaultPattern != null)
            return false;
        if (leftString != null ? !leftString.equals(that.leftString) : that.leftString != null) return false;
        if (rightString != null ? !rightString.equals(that.rightString) : that.rightString != null) return false;
        return patterns != null ? patterns.equals(that.patterns) : that.patterns == null;
    }

    @Override
    public int hashCode() {
        int result = usedPattern != null ? usedPattern.hashCode() : 0;
        result = 31 * result + (defaultPattern != null ? defaultPattern.hashCode() : 0);
        result = 31 * result + (leftString != null ? leftString.hashCode() : 0);
        result = 31 * result + (rightString != null ? rightString.hashCode() : 0);
        result = 31 * result + (patterns != null ? patterns.hashCode() : 0);
        return result;
    }

    public static void main(String[] args) {
        TimeTransferService tt = new TimeTransferService();
        tt.patterns.forEach( p ->{
            System.out.println(DateTimeFormatter.ofPattern(p).format(LocalDateTime.now()));
        });
    }
}
