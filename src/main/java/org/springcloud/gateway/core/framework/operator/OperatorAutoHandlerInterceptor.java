/*
 * Copyright 2017 ~ 2025 the original author or authors. <springcloudgateway@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springcloud.gateway.core.framework.operator;

import static org.springcloud.gateway.core.log.SmartLoggerFactory.getLogger;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springcloud.gateway.core.log.SmartLogger;

/**
 * Automatic around execution handler method interceptor.
 * 
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 */
public class OperatorAutoHandlerInterceptor implements MethodInterceptor {

	final protected SmartLogger log = getLogger(getClass());

	@Override
	public Object invoke(MethodInvocation invc) throws Throwable {
		Object targetObj = invc.getThis(); // Target object.
		// Method simple name.
		String targetMethodName = invc.getMethod().getName();
		// Method declaring class name.
		String declareClassName = invc.getMethod().getDeclaringClass().getName();

		if (!(targetObj instanceof Operator))
			throw new Error("Shouldn't be here");

		Operator<?> operator = (Operator<?>) targetObj;
		log.debug("Around operator targetObj: {}, method: {}#{}", targetObj, declareClassName, targetMethodName);

		// Call preprocessing
		if (!operator.preHandle(invc.getMethod(), invc.getArguments())) {
			log.warn("Rejected operation of {}#{}", targetObj, targetMethodName);
			return null;
		}
		// Invoke target
		Object returnObj = invc.proceed();

		// Call post processing
		operator.postHandle(invc.getMethod(), invc.getArguments(), returnObj);
		return returnObj;
	}

}