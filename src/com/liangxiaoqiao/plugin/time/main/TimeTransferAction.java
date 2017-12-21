package com.liangxiaoqiao.plugin.time.main;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;

/**
 * colter.liang
 * 2017/12/20
 * <p>
 * https://www.jetbrains.org/intellij/sdk/docs/tutorials/editor_basics/working_with_text.html
 */

public class TimeTransferAction extends AnAction {
    private final TimeTransferService ttService = ServiceManager.getService(TimeTransferService.class);

    @Override
    public void update(AnActionEvent e) {
        //Get required data keys
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        //Set visibility only in case of existing project and editor
        //TODO 待优化
        SelectionModel model = editor.getSelectionModel();
        String selectedText = model.getSelectedText();
        e.getPresentation().setVisible((project != null
                && editor != null
                && model != null
                && selectedText != null
                && !selectedText.trim().equals("")
        ));
        //重置时间格式化
        if (selectedText == null || selectedText.trim().equals("")) {
            ttService.usedPattern = null;
        }
    }

    /**
     * @param event
     */
    @Override
    public void actionPerformed(AnActionEvent event) {
        cleanService();
        //Get all the required data from data keys
        final Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        //Access document, caret, and selection
        final Document document = editor.getDocument();
        final SelectionModel selectionModel = editor.getSelectionModel();
        final int start = selectionModel.getSelectionStart();
        final int end = selectionModel.getSelectionEnd();

        SelectionModel model = editor.getSelectionModel();
        final String selectedText = model.getSelectedText();

        String result = judgeSelectWordAndParseToResult(selectedText);
        if (result == null) {
            return;
        }
        String bindResult = bind(result);
        //New instance of Runnable to make a replacement
        Runnable runnable = () -> document.replaceString(start, end, bindResult);
        //Making the replacement
        WriteCommandAction.runWriteCommandAction(project, runnable);
        // selectionModel.removeSelection();
        // 不移除选中，方便来回切换

    }

    /**
     * 选中的转成另外一种形式
     * 毫秒转时间
     * 时间转毫秒
     * 出现异常 转不了  直接返回null
     * TODO 后期增加自定义的时间格式，应该支持多个，然后选择最合适的
     *
     * @param selectWord
     * @return
     */
    private String judgeSelectWordAndParseToResult(String selectWord) {
        String old = parseWord(selectWord);
        Instant instant;
        try {
            String pattern;

            if (ttService.usedPattern != null) {
                pattern = ttService.usedPattern;
            } else {
                pattern = ttService.defaultPattern;
            }
            if (isLong(old)) {
                DateTimeFormatter formatter = ofPattern(pattern);
                instant = Instant.ofEpochMilli(Long.parseLong(old));
                ttService.usedPattern = pattern;
                return formatter.format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
            } else {
                LocalDateTime localDateTime = parseToTime(old);
                return String.valueOf(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String bind(String result) {
        return ttService.leftString + result + ttService.rightString;
    }

    private LocalDateTime parseToTime(String old) {
        if (ttService.usedPattern != null) {
            ttService.addFirst(ttService.defaultPattern);
            ttService.addFirst(ttService.usedPattern);
        } else {
            ttService.addFirst(ttService.usedPattern);
            ttService.addFirst(ttService.defaultPattern);
        }
        for (String pattern : ttService.patterns) {
            try {
                LocalDateTime value = LocalDateTime.parse(old, ofPattern(pattern));
                ttService.usedPattern = pattern;
                return value;
            } catch (Exception e) {
                //遍历所有预设的formatter
                //出错继续直到最后
                //TODO 1遍历太傻了，换成别的方式 2.预设的可以自己配置
            }
        }
        ttService.removeFirst();
        ttService.removeFirst();
        return null;
    }

    private boolean isLong(String str) {
        try {
            Long.parseLong(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * 前后引号保存
     *
     * @param word
     * @return
     */
    private String parseWord(String word) {
        if (word == null) {
            return null;
        }
        int start = word.indexOf("\"");
        int end = word.lastIndexOf("\"");
        int length = word.length();
        if (start != end) {
            ttService.leftString = "\"";
            ttService.rightString = "\"";
        } else {
            if (start != -1) {
                if (start < length / 2) {
                    ttService.leftString = "\"";
                } else {
                    ttService.rightString = "\"";
                }
            }
        }
        String trim = word.replaceAll("\"", "").trim();
        if (trim.length() > 0) {
            return trim;
        }
        return null;
    }

    private void cleanService() {
        ttService.leftString = "";
        ttService.rightString = "";
    }
}
