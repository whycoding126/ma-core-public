/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.vo.event.detector;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;

/**
 * @author Terry Packer
 *
 */
public abstract class AbstractPointEventDetectorVO<T extends AbstractPointEventDetectorVO<T>> extends AbstractEventDetectorVO<T> {

	private static final long serialVersionUID = 1L;

	public static final String XID_PREFIX = "PED_";


    private int alarmLevel;

    //Extra Fields
    protected DataPointVO dataPoint;
    private final int[] supportedDataTypes;
    
    public AbstractPointEventDetectorVO(int[] supportedDataTypes){
    	this.supportedDataTypes = supportedDataTypes;
    }
    
    public int getAlarmLevel() {
		return alarmLevel;
	}
	public void setAlarmLevel(int alarmLevel) {
		this.alarmLevel = alarmLevel;
	}

	public DataPointVO njbGetDataPoint() {
		return dataPoint;
	}

	public void njbSetDataPoint(DataPointVO dataPoint) {
		this.dataPoint = dataPoint;
	}

	/**
	 * What data types are supported
	 * @param dataType
	 * @return
	 */
    public boolean supports(int dataType) {
        return ArrayUtils.contains(supportedDataTypes, dataType);
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO#isRtnApplicable()
	 */
	@Override
	public boolean isRtnApplicable() {
		return true;
	}
	
	@Override
    public EventTypeVO getEventType() {
        return new EventTypeVO(EventType.EventTypeNames.DATA_POINT, null, dataPoint.getId(), id, getDescription(),
                alarmLevel);
    }
	
    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
    	super.jsonWrite(writer);
        writer.writeEntry("alarmLevel", AlarmLevels.CODES.getCode(alarmLevel));
    }
    
    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
    	super.jsonRead(reader, jsonObject);
    	
    	String text = jsonObject.getString("alarmLevel");
        if (text != null) {
            alarmLevel = AlarmLevels.CODES.getId(text);
            if (!AlarmLevels.CODES.isValidId(alarmLevel))
                throw new TranslatableJsonException("emport.error.ped.invalid", "alarmLevel", text,
                        AlarmLevels.CODES.getCodeList());
        }
    }
    
    protected boolean getBoolean(JsonObject json, String name) throws JsonException {
        Boolean b = json.getBoolean(name);
        if (b == null)
            throw new TranslatableJsonException("emport.error.ped.missingAttr", name);
        return b;
    }

    protected String getString(JsonObject json, String name) throws JsonException {
        String s = json.getString(name);
        if (s == null)
            throw new TranslatableJsonException("emport.error.ped.missingAttr", name);
        return s;
    }
    
    protected double getDouble(JsonObject json, String name) throws JsonException {
        Double d = json.getDouble(name);
        if (d == null)
            throw new TranslatableJsonException("emport.error.ped.missingAttr", name);
        return d;
    }

    protected int getInt(JsonObject json, String name) throws JsonException {
        Integer i = json.getInt(name);
        if (i == null)
            throw new TranslatableJsonException("emport.error.ped.missingAttr", name);
        return i;
    }
}
