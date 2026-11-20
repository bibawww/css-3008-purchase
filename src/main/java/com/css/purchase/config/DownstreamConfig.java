package com.css.purchase.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the downstream settings. Inject DownstreamProperties where the
 * outbound client port is configured. Lab 4 does not call the network.
 */
@Configuration
@EnableConfigurationProperties(DownstreamProperties.class)
public class DownstreamConfig {
}
