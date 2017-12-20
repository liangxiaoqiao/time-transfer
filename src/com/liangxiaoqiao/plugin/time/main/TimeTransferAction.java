package com.liangxiaoqiao.plugin.time.main;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * colter.liang
 * 2017/12/20
 * <p>
 * https://www.jetbrains.org/intellij/sdk/docs/tutorials/editor_basics/working_with_text.html
 */

public class TimeTransferAction extends AnAction {
    @Override
    public void update(AnActionEvent e) {
        //Get required data keys
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        //Set visibility only in case of existing project and editor
        e.getPresentation().setVisible((project != null && editor != null));
    }

    /**
     *
     * @param event
     */
    @Override
    public void actionPerformed(AnActionEvent event) {

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

        final String result = changeToResult(selectedText);
        if (result == null) {
            return;
        }
        //New instance of Runnable to make a replacement
        Runnable runnable = () -> document.replaceString(start, end, result);
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
     * @param selectWord
     * @return
     */
    private String changeToResult(String selectWord) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String old = parseWord(selectWord);
        Instant instant;
        try{
            if (isLong(old)) {
                instant = Instant.ofEpochMilli(Long.parseLong(old));
                return formatter.format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
            } else {
                LocalDateTime localDateTime = LocalDateTime.parse(old, formatter);
                return String.valueOf(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }
        }catch(Exception e){
            return null;
        }

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
     * 容错 去除前后引号
     * @param word
     * @return
     */
    private String parseWord(String word) {
        if (word == null) {
            return null;
        }
        String trim = word.replaceAll("\"", "");
        if (trim.length() > 0) {
            return trim;
        }
        return null;
    }

}
