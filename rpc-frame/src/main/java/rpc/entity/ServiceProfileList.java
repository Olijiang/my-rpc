package rpc.entity;

import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * @author Snion
 * @version 1.0
 * @description: TODO
 * @date 2024/6/1 18:13
 */
@Data
public class ServiceProfileList {

    /**
     * 服务列表
     */
    private List<ServiceProfile> serviceProfiles;

    private Long timeStamp;

    private String serviceKey;
    public ServiceProfileList() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceProfileList that)) return false;

        if (!Objects.equals(serviceProfiles, that.serviceProfiles))
            return false;
        return Objects.equals(serviceKey, that.serviceKey);
    }

    @Override
    public int hashCode() {
        int result = serviceProfiles != null ? serviceProfiles.hashCode() : 0;
        result = 31 * result + (serviceKey != null ? serviceKey.hashCode() : 0);
        return result;
    }

    public void updateTime() {
        this.timeStamp = System.currentTimeMillis();
    }
}
