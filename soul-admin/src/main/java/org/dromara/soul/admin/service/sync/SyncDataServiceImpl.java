/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.dromara.soul.admin.service.sync;

import org.apache.commons.collections4.CollectionUtils;
import org.dromara.soul.admin.listener.DataChangedEvent;
import org.dromara.soul.admin.service.AppAuthService;
import org.dromara.soul.admin.service.PluginService;
import org.dromara.soul.admin.service.RuleService;
import org.dromara.soul.admin.service.SelectorService;
import org.dromara.soul.admin.service.SyncDataService;
import org.dromara.soul.admin.transfer.PluginTransfer;
import org.dromara.soul.admin.vo.PluginVO;
import org.dromara.soul.common.dto.AppAuthData;
import org.dromara.soul.common.dto.PluginData;
import org.dromara.soul.common.dto.RuleData;
import org.dromara.soul.common.dto.SelectorData;
import org.dromara.soul.common.enums.ConfigGroupEnum;
import org.dromara.soul.common.enums.DataEventTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * The type sync data service.
 *
 * @author xiaoyu(Myth)
 */
@Service("syncDataService")
public class SyncDataServiceImpl implements SyncDataService {

    private final AppAuthService appAuthService;

    /**
     * The Plugin service.
     */
    private final PluginService pluginService;

    /**
     * The Selector service.
     */
    private final SelectorService selectorService;

    /**
     * The Rule service.
     */
    private final RuleService ruleService;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Instantiates a new Sync data service.
     *
     * @param appAuthService  the app auth service
     * @param pluginService   the plugin service
     * @param selectorService the selector service
     * @param ruleService     the rule service
     * @param eventPublisher  the event publisher
     */
    @Autowired
    public SyncDataServiceImpl(final AppAuthService appAuthService,
                               final PluginService pluginService,
                               final SelectorService selectorService,
                               final RuleService ruleService,
                               final ApplicationEventPublisher eventPublisher) {
        this.appAuthService = appAuthService;
        this.pluginService = pluginService;
        this.selectorService = selectorService;
        this.ruleService = ruleService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean syncAll(DataEventTypeEnum type) {
        List<AppAuthData> authDataList = appAuthService.listAll();

        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.APP_AUTH,
                type, authDataList));

        List<PluginData> pluginDataList = pluginService.listAll();
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.PLUGIN,
                type,
                pluginDataList));

        List<SelectorData> selectorDataList = selectorService.listAll();
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.SELECTOR,
                type,
                selectorDataList));

        List<RuleData> ruleDataList = ruleService.listAll();
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.RULE,
                type,
                ruleDataList));

        return true;
    }

    @Override
    public boolean syncPluginData(final String pluginId) {
        PluginVO pluginVO = pluginService.findById(pluginId);
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.PLUGIN, DataEventTypeEnum.UPDATE,
                Collections.singletonList(PluginTransfer.INSTANCE.mapDataTOVO(pluginVO))));
        List<SelectorData> selectorDataList = selectorService.findByPluginId(pluginId);
        if (CollectionUtils.isNotEmpty(selectorDataList)) {
            eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.SELECTOR,
                    DataEventTypeEnum.UPDATE,
                    selectorDataList));
            for (SelectorData selectData : selectorDataList) {
                List<RuleData> ruleDataList = ruleService.findBySelectorId(selectData.getId());
                eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.RULE,
                        DataEventTypeEnum.UPDATE,
                        ruleDataList));
            }
        }
        return true;
    }
}
