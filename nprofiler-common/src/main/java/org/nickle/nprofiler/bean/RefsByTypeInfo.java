package org.nickle.nprofiler.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 查询引用信息
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年02月10 11时03分
 */
@Data
public class RefsByTypeInfo {

    private JavaClass refByClass;
    private Map<JavaClass,Long> referrersMap;
    private Map<JavaClass,Long> refereesMap;

    @Data
    @NoArgsConstructor
    public class JavaClass{
        private Long id;
        private String name;
    }


}
