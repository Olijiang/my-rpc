package rpc.entity;

import lombok.Data;
import rpc.entity.ServiceProfile;

import java.util.List;

/**
 * @author Snion
 * @version 1.0
 * @description: TODO
 * @date 2024/6/1 18:13
 */
@Data
public class ServiceListProfile {

    /**
     * 权重是否发生变化, 这个值在 watch 监听到节点数量发生变化时为 true, 权重在运行中是不可修改的
     */
    private boolean weightChange;

    /**
     * 信用值是否发生变化, 这个值在 watch 监听到节点的信用值发生变化时为 true
     */
    private boolean creditChange;

    /**
     * 服务列表
     */
    private List<ServiceProfile> serviceProfiles;


    public ServiceListProfile() {
        this.weightChange = true;
        this.creditChange = true;
    }
}
