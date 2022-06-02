
package com.bankbazaar.webclient.core.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieData implements Serializable
{
    @JsonProperty(value = "Title")
    @SerializedName(value = "Title")
    private String title;

    @JsonProperty(value = "Year")
    @SerializedName(value = "Year")
    private String year;

    @JsonProperty(value = "Rated")
    @SerializedName(value = "Rated")
    private String rated;

    @JsonProperty(value = "Released")
    @SerializedName(value = "Released")
    private String released;

    @JsonProperty(value = "Runtime")
    @SerializedName(value = "Runtime")
    private String runtime;

    @JsonProperty(value = "Genre")
    @SerializedName(value = "Genre")
    private String genre;

    @JsonProperty(value = "Director")
    @SerializedName(value = "Director")
    private String director;

    @JsonProperty(value = "Writer")
    @SerializedName(value = "Writer")
    private String writer;

    @JsonProperty(value = "Actors")
    @SerializedName(value = "Actors")
    private String actors;

    @JsonProperty(value = "Plot")
    @SerializedName(value = "Plot")
    private String plot;

    @JsonProperty(value = "Language")
    @SerializedName(value = "Language")
    private String language;

    @JsonProperty(value = "Country")
    @SerializedName(value = "Country")
    private String country;

    @JsonProperty(value = "Awards")
    @SerializedName(value = "Awards")
    private String awards;

    @JsonProperty(value = "Poster")
    @SerializedName(value = "Poster")
    private String poster;

    @JsonProperty(value = "Ratings")
    @SerializedName(value = "Ratings")
    private List<Rating> ratings;

    @JsonProperty(value = "Metascore")
    @SerializedName(value = "Metascore")
    private String metaScore;

    @JsonProperty(value = "imdbRating")
    @SerializedName(value = "imdbRating")
    private String imdbRating;

    @JsonProperty(value = "imdbVotes")
    @SerializedName(value = "imdbVotes")
    private String imdbVotes;

    @JsonProperty(value = "imdbID")
    @SerializedName(value = "imdbID")
    private String imdbID;

    @JsonProperty(value = "Type")
    @SerializedName(value = "Type")
    private String type;

    @JsonProperty(value = "DVD")
    @SerializedName(value = "DVD")
    private String dvd;

    @JsonProperty(value = "BoxOffice")
    @SerializedName(value = "BoxOffice")
    private String boxOffice;

    @JsonProperty(value = "Production")
    @SerializedName(value = "Production")
    private String production;

    @JsonProperty(value = "Website")
    @SerializedName(value = "Website")
    private String website;

    @JsonProperty(value = "Response")
    @SerializedName(value = "Response")
    private Boolean response;

    private final static long serialVersionUID = 3006711607587363498L;

}
