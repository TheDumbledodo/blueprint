package io.github.dumbledodo.blueprint.data;

import lombok.*;

@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Pair<K, V> {

    private K key;
    private V value;
}