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
		if (applicationContext == null) {
			applicationContext = new ClassPathXmlApplicationContext(
					"ctx-midpoint-jaspersoft.xml");
		}

		if (reportPort == null) {
			reportPort = (ReportPortType) applicationContext
					.getBean("reportPort");
		}

	}
	
	@Override
	public JRQueryExecuter createQueryExecuter(JasperReportsContext jasperReportsContext, JRDataset dataset,
			Map<String, ? extends JRValueParameter> parameters) throws JRException {
		return new MidPointRemoteQueryExecutor(jasperReportsContext, dataset, parameters, reportPort);
	}

	@Override
	public boolean supportsQueryParameterType(String className) {
		return true;
	}
	

}
