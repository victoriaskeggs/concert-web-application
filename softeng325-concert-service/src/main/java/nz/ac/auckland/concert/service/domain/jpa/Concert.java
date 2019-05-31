package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a concert
 */
@Entity(name = "CONCERTS")
public class Concert implements Comparable<Concert> {

    @Id
    @GeneratedValue
    private Long _id;

    @Column(nullable = false)
    private String _title;

    @ElementCollection
    @CollectionTable(name = "CONCERT_DATES")
    @Column(nullable = false)
    private Set<LocalDateTime> _dates = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "CONCERT_TARIFFS")
    @MapKeyColumn(name = "PRICE_BAND")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(nullable = false)
    private Map<PriceBand, BigDecimal> _ticketPrices = new HashMap<>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "CONCERT_PERFORMER",
            joinColumns = @JoinColumn(name = "CONCERT_ID"),
            inverseJoinColumns = @JoinColumn(name = "PERFORMER_ID")
    )
    @JoinColumn(nullable = false)
    private Set<Performer> _performers = new HashSet<>();

    public Concert(Long id, String title, Set<LocalDateTime> dates, Map<PriceBand, BigDecimal> ticketPrices,
                   Set<Performer> performers) {
        _id = id;
        _title = title;
        _dates = dates;
        _ticketPrices = ticketPrices;
        _performers = performers;
    }

    public Concert() {}

    /**
     * Sorts Concert objects alphabetically by title
     * @param other
     * @return
     */
    @Override
    public int compareTo(Concert other) {
        return _title.compareTo(other._title);
    }

    public Long getId() {
        return _id;
    }

    public String getTitle() {
        return _title;
    }

    public Set<LocalDateTime> getDates() {
        return _dates;
    }

    public Map<PriceBand, BigDecimal> getTicketPrices() {
        return _ticketPrices;
    }

    public Set<Performer> getPerformers() {
        return _performers;
    }

    public void setId(Long id) {
        this._id = id;
    }

    public void setTitle(String title) {
        this._title = title;
    }

}
