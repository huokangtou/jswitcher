/*
 * Copyright 2019 xincao9@gmail.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.xincao9.jswitcher.service.service;

import com.github.xincao9.jswitcher.api.service.SwitcherService;
import com.github.xincao9.jswitcher.api.vo.QoS;
import com.github.xincao9.jswitcher.api.vo.Switcher;
import com.github.xincao9.jswitcher.service.Configure;
import com.github.xincao9.jswitcher.service.dao.SwitcherDAO;
import com.github.xincao9.jswitcher.api.exception.KeyNotFoundException;
import com.github.xincao9.jswitcher.api.exception.ParameterInvalidException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 开关服务
 *
 * @author xincao9@gmail.com
 */
public class SwitcherServiceImpl implements SwitcherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwitcherService.class);

    private final Map<String, Switcher> keyAndSwitcher = new ConcurrentHashMap();
    private final SwitcherDAO switcherDAO;
    private final Configure configre;

    /**
     * 构造器
     *
     * @param configre 配置
     * @param switcherDAO 开关仓库
     */
    public SwitcherServiceImpl(Configure configre, SwitcherDAO switcherDAO) {
        this.configre = configre;
        this.switcherDAO = switcherDAO;
    }

    /**
     * 注册开关（开发使用）
     *
     * @param key 键值
     * @param open 开关状态
     * @param describe 描述
     * @param qos 服务质量
     */
    @Override
    public void register(String key, Boolean open, String describe, QoS qos) {
        String k = String.valueOf(key);
        if (!this.keyAndSwitcher.containsKey(k)) {
            Switcher switcher = getSwitcherByKey(k);
            if (switcher == null) {
                if (!this.keyAndSwitcher.containsKey(k)) {
                    switcher = new Switcher();
                    switcher.setKey(k);
                    switcher.setOpen(open);
                    switcher.setDescribe(describe);
                    switcher.setQos(qos);
                    this.keyAndSwitcher.put(k, switcher);
                    LOGGER.warn("new registration switch {}", switcher.toString());
                }
            } else {
                this.keyAndSwitcher.put(k, switcher);
                LOGGER.warn("load switch information {}", switcher.toString());
            }
        }
    }

    /**
     * 是开放状态（开发使用）
     *
     * @param key 键值
     * @return 是/否
     */
    @Override
    public Boolean isOpen(String key) {
        String k = String.valueOf(key);
        if (this.keyAndSwitcher.containsKey(k)) {
            return this.keyAndSwitcher.get(k).getOpen();
        }
        LOGGER.warn("this switch has not been registered in the application. key = {}", k);
        return false;
    }

    /**
     * 关闭状态（开发使用）
     *
     * @param key 键值
     * @return 是/否
     */
    @Override
    public Boolean isClose(String key) {
        return !isOpen(key);
    }

    /**
     * 检查开关状态（操作和维护使用）
     *
     * @param key 键值
     * @return 开关状态
     */
    @Override
    public Boolean check(String key) {
        String k = String.valueOf(key);
        if (StringUtils.isBlank(k)) {
            throw new ParameterInvalidException("key can not be empty!");
        }
        if (!this.keyAndSwitcher.containsKey(k)) {
            throw new KeyNotFoundException(String.format("key = %s can't find!", k));
        }
        return this.keyAndSwitcher.get(k).getOpen();
    }

    /**
     * 开关（操作和维护使用）
     *
     * @param key 键值
     */
    @Override
    public void on(String key) {
        String k = String.valueOf(key);
        if (StringUtils.isBlank(k)) {
            throw new ParameterInvalidException("key can not be empty!");
        }
        if (!this.keyAndSwitcher.containsKey(k)) {
            throw new KeyNotFoundException(String.format("key = %s can't find!", k));
        }
        this.keyAndSwitcher.get(k).setOpen(true);
    }

    /**
     * 关闭开关（操作和维护使用）
     *
     * @param key 键值
     */
    @Override
    public void off(String key) {
        String k = String.valueOf(key);
        if (StringUtils.isBlank(k)) {
            throw new ParameterInvalidException("key can not be empty!");
        }
        if (!this.keyAndSwitcher.containsKey(k)) {
            throw new KeyNotFoundException(String.format("key = %s can't find!", k));
        }
        this.keyAndSwitcher.get(k).setOpen(false);
    }

    /**
     * 设置开关状态并永久保存（操作和维护）
     *
     * @param key 键值
     * @param open 开关状态
     */
    @Override
    public void set(String key, Boolean open) {
        String k = String.valueOf(key);
        if (StringUtils.isBlank(k)) {
            throw new ParameterInvalidException("key can not be empty!");
        }
        if (!this.keyAndSwitcher.containsKey(k)) {
            throw new KeyNotFoundException(String.format("key = %s can't find!", k));
        }
        this.keyAndSwitcher.get(k).setOpen(open);
        Switcher switcher = getSwitcherByKey(k);
        if (switcher != null) {
            this.switcherDAO.changeStatusByKey(k, !open, open);
        } else {
            this.switcherDAO.insert(this.keyAndSwitcher.get(k)); // resolve repeated insertion problems by using key as a unique index
        }
    }

    /**
     * 查看开关列表（操作和维护使用）
     *
     * @return 开关列表
     */
    @Override
    public List<Switcher> list() {
        List<Switcher> switcheres = new ArrayList(this.keyAndSwitcher.values());
        return switcheres;
    }

    /**
     * 获取开关
     *
     * @param key 键值
     * @return 开关
     */
    public Switcher getSwitcherByKey(String key) {
        if (StringUtils.isNotEmpty(key)) {
            return this.switcherDAO.selectByKey(key);
        }
        return null;
    }

}
