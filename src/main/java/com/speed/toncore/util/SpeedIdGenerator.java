package com.speed.toncore.util;

import com.speed.javacommon.util.SpeedUUIDGenerator;
import com.speed.toncore.util.enums.Modules;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

public class SpeedIdGenerator implements IdentifierGenerator {

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object obj) throws HibernateException {
		return String.format("%s_%s", Modules.getPrefixByModule(obj.getClass().getSimpleName()), SpeedUUIDGenerator.generate());
	}
}