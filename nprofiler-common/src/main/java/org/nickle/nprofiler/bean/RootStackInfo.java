package org.nickle.nprofiler.bean;

import com.sun.tools.hat.internal.model.StackFrame;
import lombok.Data;

/**
 * 根调用栈信息
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月17 22时53分
 */
@Data
public class RootStackInfo {
    private StackFrame[] frames;
    private String description;
}


