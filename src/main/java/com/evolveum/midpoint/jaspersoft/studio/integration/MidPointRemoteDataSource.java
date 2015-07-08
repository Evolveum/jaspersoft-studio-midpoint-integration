/* Copyright (c) 2014-2015 Evolveum

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. */
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
