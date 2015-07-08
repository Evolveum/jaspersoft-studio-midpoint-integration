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

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.query.JRAbstractQueryExecuter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.internal.dtree.ObjectNotFoundException;

import com.evolveum.midpoint.model.client.ModelClientUtil;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.GetOperationOptionsType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.SelectorQualifiedGetOptionType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.SelectorQualifiedGetOptionsType;
import com.evolveum.midpoint.xml.ns._public.common.audit_3.AuditEventRecordListType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ReportParameterType;
import com.evolveum.midpoint.xml.ns._public.report.report_3.RemoteReportParameterType;
import com.evolveum.midpoint.xml.ns._public.report.report_3.RemoteReportParametersType;
import com.evolveum.midpoint.xml.ns._public.report.report_3.ReportPortType;
//import com.evolveum.midpoint.schema.constants.SchemaConstants;

public class MidPointRemoteQueryExecutor extends JRAbstractQueryExecuter {

	private static final Trace LOGGER = TraceManager.getTrace(MidPointRemoteQueryExecutor.class);
	private String script;
	private ReportPortType reportPort;
	private String queryString;

	public static final String NS_C = "http://midpoint.evolveum.com/xml/ns/public/common/common-3";
	private static final String NS_AUDIT = "http://midpoint.evolveum.com/xml/ns/public/common/audit-3";
	public static final String NS_REPORT = "http://midpoint.evolveum.com/xml/ns/public/report/report-3";

		protected String getParsedScript(String script) {
		String normalized = script.replace("<code>", "");
		return normalized.replace("</code>", "");
	}

	@Override
	protected void parseQuery() {
		String s = dataset.getQuery().getText();
		LOGGER.trace("query: " + s);
		if (StringUtils.isEmpty(s)) {
			queryString = null;
		} else {
				if (s.startsWith("<filter")) {
					queryString = getStringQuery();//getParsedQuery(s, expressionParameters);
				} else if (s.startsWith("<code")) {
					script = getParsedScript(s);
				}
		}

	}

	private Object createRemoteParamValue(String paramName, Object v, boolean audit) {
		if (XsdTypeMapper.getJavaToXsdMapping(v.getClass()) != null) {
			return v;
		}

		QName elementName = null;
		if (audit){
			elementName = new QName(NS_AUDIT, paramName);
		} else {
			elementName = new QName(NS_C, paramName);
		}
		
		JAXBElement<Object> e = ModelClientUtil.toJaxbElement(elementName, v);
		return e;

	}

	@SuppressWarnings("rawtypes")
	private RemoteReportParametersType converToReportParameterType(boolean audit)
			throws SchemaException {
		RemoteReportParametersType remoteParams = new RemoteReportParametersType();

		for (JRParameter jrParam : dataset.getParameters()) {
			if (jrParam.isSystemDefined()) {
				continue;
			}
			
			if (!jrParam.isForPrompting() && audit){
				continue;
			}

			RemoteReportParameterType remoteParam = new RemoteReportParameterType();
			remoteParam.setParameterName(jrParam.getName());

			Object value = getParameterValue(jrParam.getName());
			if (value == null) {
				remoteParams.getRemoteParameter().add(remoteParam);
				continue;
			}

			ReportParameterType paramValue = new ReportParameterType();
			if (List.class.isAssignableFrom(value.getClass())) {
				for (Object v : (List) value) {
					paramValue.getAny().add(createRemoteParamValue(jrParam.getName(), v, audit));
				}
			} else {
				paramValue.getAny().add(createRemoteParamValue(jrParam.getName(), value, audit));
			}
			remoteParam.setParameterValue(paramValue);
			remoteParams.getRemoteParameter().add(remoteParam);

		}
		return remoteParams;
	}

	public static <T> JAXBElement<T> toJaxbElement(QName name, Class<T> clazz,
			T value) {
		return new JAXBElement<T>(name, clazz, value);
	}

	protected MidPointRemoteQueryExecutor(
			JasperReportsContext jasperReportsContext, JRDataset dataset,
			Map<String, ? extends JRValueParameter> parametersMap, ReportPortType reportPort) {
		super(jasperReportsContext, dataset, parametersMap);
		this.reportPort = reportPort;
		parseQuery();
	}

	private String getStringQuery() {
		if (dataset.getQuery() == null) {
			// query = null;
			return null;
		}
		return dataset.getQuery().getText();
	}

	@Override
	public void close() {
	}

	@Override
	public JRDataSource createDatasource() throws JRException {

		ObjectListType results = null;
		try {
			if (queryString == null && script == null) {
				throw new JRException(
						"Neither query, nor script defined in the report.");
			}

			if (queryString != null) {
				results = reportPort.processReport(queryString, converToReportParameterType(false), createRawOption());
			} else {
				if (script.contains("AuditEventRecord")) {
					AuditEventRecordListType auditResults = reportPort.evaluateAuditScript(script, converToReportParameterType(true));
					return new JRBeanCollectionDataSource(auditResults.getObject());
				} else {

					RemoteReportParametersType reportParamters = converToReportParameterType(false);
					LOGGER.debug("coverted to report parameters: {}",reportParamters);
					results = reportPort.evaluateScript(script, reportParamters);
				}
			}
		} catch (SchemaException | ObjectNotFoundException e) {
			throw new JRException(e);
		}
		return new MidPointRemoteDataSource(results.getObject());

	}
	
	private SelectorQualifiedGetOptionsType createRawOption(){
		SelectorQualifiedGetOptionsType options = new SelectorQualifiedGetOptionsType();
		SelectorQualifiedGetOptionType option = new SelectorQualifiedGetOptionType();
		GetOperationOptionsType getOptions = new GetOperationOptionsType();
		getOptions.setRaw(Boolean.TRUE);
		option.setOptions(getOptions);
		options.getOption().add(option);
		return options;
	}

	@Override
	public boolean cancelQuery() throws JRException {
		throw new UnsupportedOperationException(
				"QueryExecutor.cancelQuery() not supported");
	}

	@Override
	protected String getParameterReplacement(String parameterName) {
		throw new UnsupportedOperationException(
				"QueryExecutor.getParameterReplacement() not supported");
	}

}
