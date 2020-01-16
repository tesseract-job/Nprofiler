package org.nickle.nprofiler.bean;

import lombok.Data;

import java.util.List;

/**
 * class详细信息实体
 * @author wesley
 * @create 2020-01-16
 */
@Data
public class ClassInfo {

    private String name;
    private String superclass;
    private String loader;
    private String signers;
    private String protectionDomain;
    private List<JavaField> fields;
    private List<JavaStatic> statics;
    private List<String> subclasses;


    @Data
    public class JavaField {
        private String name;
        private String signature;
    }
    @Data
    public class JavaStatic {
        private JavaField field;
        private String value;
    }

}
