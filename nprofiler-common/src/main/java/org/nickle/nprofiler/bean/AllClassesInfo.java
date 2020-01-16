package org.nickle.nprofiler.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 所有class信息查询实体
 * @author wesley
 * @create 2020-01-16
 */
@Data
public class AllClassesInfo {

    private Map<String,List<JavaClass>> info;

    @Data
    @NoArgsConstructor
    public class JavaClass{
        private long id;
        private String name;
    }

}
