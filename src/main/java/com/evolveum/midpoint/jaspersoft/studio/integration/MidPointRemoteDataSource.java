package com.evolveum.midpoint.jaspersoft.studio.integration;

import java.util.Collection;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

public class MidPointRemoteDataSource extends JRBeanCollectionDataSource {

	public MidPointRemoteDataSource(Collection<?> beanCollection) {
		super(beanCollection);
	}
	
	
	@Override
	public Object getFieldValue(JRField field) throws JRException {
		Object value = super.getFieldValue(field);
		
		if (field.getValueClass().equals(PolyString.class)){
			if (value instanceof PolyStringType){
				if (((PolyStringType) value).getContent().size() != 0){
					return new PolyString((String) ((PolyStringType) value).getContent().get(0));
				} else {
					return null;
				}
			}
		}
		
		if (field.getValueClass().equals(String.class)){
			if (value instanceof PolyString){
				return ((PolyString) value).getOrig();
			} else if (value instanceof PolyStringType){
				if (((PolyStringType) value).getContent().size() != 0){
					return ((PolyStringType) value).getContent().get(0);
				} else {
					return null;
				}
			}
		}
		
		return value;
	}
	

}
