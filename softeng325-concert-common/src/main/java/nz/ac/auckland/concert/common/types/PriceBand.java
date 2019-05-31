package nz.ac.auckland.concert.common.types;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Enumerated type for classifying seats according to price bands.
 *
 */
@XmlRootElement
public enum PriceBand {
	PriceBandA, PriceBandB, PriceBandC;
}
