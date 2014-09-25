package com.sina.cloudstorage;

import java.util.concurrent.atomic.AtomicInteger;


public class SDKGlobalConfiguration {

	// ///////////////////// System Properties ///////////////////////
	public static final String DISABLE_CERT_CHECKING_SYSTEM_PROPERTY = "com.amazonaws.sdk.disableCertChecking";
	/**
	 * System property used when starting up the JVM to enable the default
	 * metrics collected by the AWS SDK, which uploads the derived statistics to
	 * Amazon CloudWatch.
	 * 
	 * <pre>
	 * Example:
	 *  -Dcom.amazonaws.sdk.enableDefaultMetrics
	 * </pre>
	 */
	public static final String DEFAULT_METRICS_SYSTEM_PROPERTY = "com.amazonaws.sdk.enableDefaultMetrics";
	/** System property name for the AWS access key ID */
	public static final String ACCESS_KEY_SYSTEM_PROPERTY = "aws.accessKeyId";
	/** System property name for the AWS secret key */
	public static final String SECRET_KEY_SYSTEM_PROPERTY = "aws.secretKey";
	/**
	 * System property for overriding the Amazon EC2 Instance Metadata Service
	 * endpoint.
	 */
	public static final String EC2_METADATA_SERVICE_OVERRIDE_SYSTEM_PROPERTY = "com.amazonaws.sdk.ec2MetadataServiceEndpointOverride";
	/**
	 * @deprecated by {@link #DEFAULT_METRICS_SYSTEM_PROPERTY}.
	 * 
	 *             Internal system property to enable timing info collection.
	 */
	@Deprecated
	public static final String PROFILING_SYSTEM_PROPERTY = "com.amazonaws.sdk.enableRuntimeProfiling";

	// ///////////////////// Environment Variables ///////////////////////
	/** Environment variable name for the AWS access key ID */
	public static final String ACCESS_KEY_ENV_VAR = "AWS_ACCESS_KEY_ID";

	/** Alternate environment variable name for the AWS access key ID */
	public static final String ALTERNATE_ACCESS_KEY_ENV_VAR = "AWS_ACCESS_KEY";

	/** Environment variable name for the AWS secret key */
	public static final String SECRET_KEY_ENV_VAR = "AWS_SECRET_KEY";

	/** Alternate environment variable name for the AWS secret key */
	public static final String ALTERNATE_SECRET_KEY_ENV_VAR = "AWS_SECRET_ACCESS_KEY";

	/**
	 * globalTimeOffset is a time offset that is used to globally adjust the
	 * client clock skew. Java SDK already provides timeOffset and accessor
	 * methods in {@link Request} class but those are used per request, whereas
	 * this variable will adjust clock skew globally. Java SDK detects clock
	 * skew errors and adjusts global clock skew automatically.
	 */
	private static final AtomicInteger globalTimeOffset = new AtomicInteger(0);

	/**
	 * Sets the global time offset. If this value is set then all the subsequent
	 * requests will use this value to generate timestamps. To adjust clock skew
	 * per request use {@link Request#setTimeOffset(int)}
	 * 
	 * @param timeOffset
	 *            the time difference between local client and server
	 */
	public static void setGlobalTimeOffset(int timeOffset) {
		globalTimeOffset.set(timeOffset);
	}

	/**
	 * Gets the global time offset. See {@link Request#getTimeOffset()} if
	 * global time offset is not set.
	 * 
	 * @return globalTimeOffset an AtomicInteger that holds the value of time
	 *         offset
	 */
	public static int getGlobalTimeOffset() {
		return globalTimeOffset.get();
	}

}
