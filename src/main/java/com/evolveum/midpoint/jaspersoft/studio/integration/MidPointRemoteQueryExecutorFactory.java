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

import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.evolveum.midpoint.xml.ns._public.report.report_3.ReportPortType;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.AbstractQueryExecuterFactory;
import net.sf.jasperreports.engine.query.JRQueryExecuter;

public class MidPointRemoteQueryExecutorFactory extends AbstractQueryExecuterFactory{


	public final static String PARAMETER_MIDPOINT_CONNECTION = "MIDPOINT_CONNECTION";
	
	private ReportPortType reportPort;
	private ClassPathXmlApplicationContext applicationContext;
	
	private final static Object[] MIDPOINT_BUILTIN_PARAMETERS = {
		PARAMETER_MIDPOINT_CONNECTION, "midpoint.connection"
		};
	
	
	@Override
	public Object[] getBuiltinParameters() {
		return null;
	}

	
	public MidPointRemoteQueryExecutorFactory() {
		
	}
	
	@Override
	public JRQueryExecuter createQueryExecuter(JasperReportsContext jasperReportsContext, JRDataset dataset,
			Map<String, ? extends JRValueParameter> parameters) throws JRException {
		if (applicationContext == null) {
			applicationContext = new ClassPathXmlApplicationContext(
					"ctx-midpoint-jaspersoft.xml");
		}

		if (reportPort == null) {
			reportPort = (ReportPortType) applicationContext
					.getBean("reportPort");
		}

		return new MidPointRemoteQueryExecutor(jasperReportsContext, dataset, parameters, reportPort);
	}

	@Override
	public boolean supportsQueryParameterType(String className) {
		return true;
	}
	
	
	

}
