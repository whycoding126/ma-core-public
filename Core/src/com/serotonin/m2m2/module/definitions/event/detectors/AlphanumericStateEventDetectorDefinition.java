/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.module.definitions.event.detectors;

import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;
import com.serotonin.m2m2.vo.event.detector.AlphanumericStateDetectorVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.detectors.AbstractEventDetectorModel;

/**
 * @author Terry Packer
 *
 */
public class AlphanumericStateEventDetectorDefinition extends PointEventDetectorDefinition{

	public static final String TYPE_NAME = "ALPHANUMERIC_STATE";
		
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.EventDetectorDefinition#getEventDetectorSubTypeName()
	 */
	@Override
	public String getEventDetectorTypeName() {
		return TYPE_NAME;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.EventDetectorDefinition#getDescriptionKey()
	 */
	@Override
	public String getDescriptionKey() {
		return "pointEdit.detectors.state";
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.EventDetectorDefinition#createEventDetectorVO()
	 */
	@Override
	protected AbstractEventDetectorVO<?> createEventDetectorVO() {
		return new AlphanumericStateDetectorVO();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.EventDetectorDefinition#createModel(com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO)
	 */
	@Override
	public AbstractEventDetectorModel<?> createModel(
			AbstractEventDetectorVO<?> vo) {
		return null;
	}

}
