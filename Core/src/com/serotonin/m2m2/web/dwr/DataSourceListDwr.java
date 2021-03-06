/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.web.dwr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.db.pair.StringStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.DataPointNameComparator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.dataSource.mock.MockDataSourceVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.comparators.StringStringPairComparator;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

/**
 * @author Matthew Lohbihler
 */
public class DataSourceListDwr extends BaseDwr {
    @DwrPermission(user = true)
    public ProcessResult init() {
        ProcessResult response = new ProcessResult();

        User user = Common.getUser();
        List<DataSourceVO<?>> allDataSources = Common.runtimeManager.getDataSources();

        if (Permissions.hasAdmin(user)) {
            response.addData("dataSources", allDataSources);

            List<StringStringPair> translatedTypes = new ArrayList<>();
            for (String type : ModuleRegistry.getDataSourceDefinitionTypes())
                translatedTypes.add(new StringStringPair(type, translate(ModuleRegistry.getDataSourceDefinition(type)
                        .getDescriptionKey())));
            StringStringPairComparator.sort(translatedTypes);
            response.addData("types", translatedTypes);
        }
        else {
            List<DataSourceVO<?>> dataSources = new ArrayList<>();
            for (DataSourceVO<?> ds : allDataSources) {
                if (Permissions.hasDataSourcePermission(user, ds))
                    dataSources.add(ds);
            }

            response.addData("dataSources", dataSources);
        }

        return response;
    }

    @DwrPermission(user = true)
    public Map<String, Object> toggleDataSource(int dataSourceId) {
        DataSourceVO<?> dataSource = Common.runtimeManager.getDataSource(dataSourceId);

        Permissions.ensureDataSourcePermission(Common.getUser(), dataSource);

        Map<String, Object> result = new HashMap<>();

        dataSource.setEnabled(!dataSource.isEnabled());
        Common.runtimeManager.saveDataSource(dataSource);

        result.put("enabled", dataSource.isEnabled());
        result.put("id", dataSourceId);
        return result;
    }

    public List<DataPointVO> getPointsForDataSource(int dataSourceId) {
        return DataPointDao.instance.getDataPoints(dataSourceId, DataPointNameComparator.instance);
    }

    @DwrPermission(user = true)
    public int deleteDataSource(int dataSourceId) {
        Permissions.ensureDataSourcePermission(Common.getUser(), dataSourceId);
        Common.runtimeManager.deleteDataSource(dataSourceId);
        return dataSourceId;
    }

    @DwrPermission(user = true)
    public ProcessResult toggleDataPoint(int dataPointId) {
        DataPointVO dataPoint = DataPointDao.instance.getDataPoint(dataPointId);
        Permissions.ensureDataSourcePermission(Common.getUser(), dataPoint.getDataSourceId());

        dataPoint.setEnabled(!dataPoint.isEnabled());
        Common.runtimeManager.saveDataPoint(dataPoint);

        ProcessResult response = new ProcessResult();
        response.addData("id", dataPointId);
        response.addData("enabled", dataPoint.isEnabled());
        return response;
    }

    @DwrPermission(user = true)
    public ProcessResult dataSourceInfo(int dataSourceId) {
        DataSourceVO<?> ds = DataSourceDao.instance.getDataSource(dataSourceId);
        Permissions.ensureDataSourcePermission(Common.getUser(), ds);
        ProcessResult response = new ProcessResult();

        String name = StringUtils.abbreviate(
                TranslatableMessage.translate(getTranslations(), "common.copyPrefix", ds.getName()), 40);

        response.addData("name", name);
        response.addData("xid", DataSourceDao.instance.generateUniqueXid());
        response.addData("deviceName", name);

        return response;
    }

    @DwrPermission(user = true)
    public ProcessResult copyDataSource(int dataSourceId, String name, String xid, String deviceName) {
        Permissions.ensureDataSourcePermission(Common.getUser(), dataSourceId);
        ProcessResult response = new ProcessResult();

        // Create a dummy data source with which to do the validation.
        DataSourceVO<?> ds = new MockDataSourceVO();
        ds.setName(name);
        ds.setXid(xid);
        ds.validate(response);

        if (!response.getHasMessages()) {
            int dsId = DataSourceDao.instance.copyDataSource(dataSourceId, name, xid, deviceName);
            response.addData("newId", dsId);
        }

        return response;
    }

    @DwrPermission(user = true)
    public String exportDataSourceAndPoints(int dataSourceId) {
        Map<String, Object> data = new LinkedHashMap<>();
        List<DataSourceVO<?>> dss = new ArrayList<>();
        dss.add(DataSourceDao.instance.getDataSource(dataSourceId));
        data.put(EmportDwr.DATA_SOURCES, dss);
        data.put(EmportDwr.DATA_POINTS, DataPointDao.instance.getDataPoints(dataSourceId, null));
        return EmportDwr.export(data, 3);
    }
}
