package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.Genre;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a performer
 */
@Entity(name = "PERFORMERS")
public class Performer {

    @Id
    @GeneratedValue
    private Long _id;

    @Enumerated(EnumType.STRING)
    private Genre _genre;

    private String _imageName;

    @Column(nullable = false)
    private String _name;

    @ManyToMany(mappedBy = "_performers")
    private Set<Concert> _concerts = new HashSet<>();

    public Performer() {}

    public Performer(Long id, String name, String imageName, Genre genre, Set<Concert> concerts) {
            this._id = id;
            this._name = name;
            _imageName = imageName;
            this._genre = genre;
            this._concerts = concerts;
    }

    public Long getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public Genre getGenre() {
        return _genre;
    }

    public Set<Concert> getConcerts() {
        return _concerts;
    }

    public void setId(Long id) {
        this._id = id;
    }

    public void setName(String name) {
        this._name = name;
    }

    public void setGenre(Genre genre) {
        this._genre = genre;
    }

    public String getImageName() {
        return _imageName;
    }

    public void setImageName(String imageName) {
        this._imageName = imageName;
    }
}
