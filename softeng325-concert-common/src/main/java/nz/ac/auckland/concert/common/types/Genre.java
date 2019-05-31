package nz.ac.auckland.concert.common.types;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Enumerated type for classifying performers.
 *
 */
@XmlRootElement
public enum Genre {Pop, HipHop, RhythmAndBlues, Acappella, Metal, Rock}