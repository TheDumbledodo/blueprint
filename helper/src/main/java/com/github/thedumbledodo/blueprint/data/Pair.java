package com.github.thedumbledodo.blueprint.data;

import lombok.*;

@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Pair<K, V> {

    private K key;
    private V value;
}