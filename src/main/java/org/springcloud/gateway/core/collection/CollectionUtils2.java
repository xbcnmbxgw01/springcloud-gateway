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
package org.springcloud.gateway.core.collection;

import static org.springcloud.gateway.core.lang.Assert2.isTrue;
import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.EnumerationUtils;

import org.springcloud.gateway.core.collection.multimap.MultiValueMap;
import org.springcloud.gateway.core.function.ProcessFunction;
import org.springcloud.gateway.core.lang.Assert2;
import org.springcloud.gateway.core.lang.ObjectUtils2;

/**
 * Miscellaneous collection utility methods. Mainly for internal use within the
 * framework.
 * 
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 */
public abstract class CollectionUtils2 extends CollectionUtils {

    // ----------------------------------------------
    // --- Spring collection util methods. ---
    // ----------------------------------------------

    /**
     * Return {@code true} if the supplied Map is {@code null} or empty.
     * Otherwise, return {@code false}.
     * 
     * @param map
     *            the Map to check
     * @return whether the given Map is empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    /**
     * Convert the supplied array into a List. A primitive array gets converted
     * into a List of the appropriate wrapper type.
     * <p>
     * <b>NOTE:</b> Generally prefer the standard {@link Arrays#asList} method.
     * This {@code arrayToList} method is just meant to deal with an incoming
     * Object value that might be an {@code Object[]} or a primitive array at
     * runtime.
     * <p>
     * A {@code null} source value will be converted to an empty List.
     * 
     * @param source
     *            the (potentially primitive) array
     * @return the converted List result
     * @see ObjectUtils2#toObjectArray(Object)
     * @see Arrays#asList(Object[])
     */
    @SuppressWarnings("rawtypes")
    public static List arrayToList(Object source) {
        return Arrays.asList(ObjectUtils2.toObjectArray(source));
    }

    /**
     * Merge the given array into the given Collection.
     * 
     * @param array
     *            the array to merge (may be {@code null})
     * @param collection
     *            the target Collection to merge the array into
     */
    @SuppressWarnings("unchecked")
    public static <E> void mergeArrayIntoCollection(Object array, Collection<E> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection must not be null");
        }
        Object[] arr = ObjectUtils2.toObjectArray(array);
        for (Object elem : arr) {
            collection.add((E) elem);
        }
    }

    /**
     * Merge the given Properties instance into the given Map, copying all
     * properties (key-value pairs) over.
     * <p>
     * Uses {@code Properties.propertyNames()} to even catch default properties
     * linked into the original Properties instance.
     * 
     * @param props
     *            the Properties instance to merge (may be {@code null})
     * @param map
     *            the target Map to merge the properties into
     */
    @SuppressWarnings("unchecked")
    public static <K, V> void mergePropertiesIntoMap(Properties props, Map<K, V> map) {
        if (map == null) {
            throw new IllegalArgumentException("Map must not be null");
        }
        if (props != null) {
            for (Enumeration<?> en = props.propertyNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                Object value = props.get(key);
                if (value == null) {
                    // Allow for defaults fallback or potentially overridden
                    // accessor...
                    value = props.getProperty(key);
                }
                map.put((K) key, (V) value);
            }
        }
    }

    /**
     * Check whether the given Iterator contains the given element.
     * 
     * @param iterator
     *            the Iterator to check
     * @param element
     *            the element to look for
     * @return {@code true} if found, {@code false} else
     */
    public static boolean contains(Iterator<?> iterator, Object element) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                Object candidate = iterator.next();
                if (ObjectUtils2.nullSafeEquals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether the given Enumeration contains the given element.
     * 
     * @param enumeration
     *            the Enumeration to check
     * @param element
     *            the element to look for
     * @return {@code true} if found, {@code false} else
     */
    public static boolean contains(Enumeration<?> enumeration, Object element) {
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                Object candidate = enumeration.nextElement();
                if (ObjectUtils2.nullSafeEquals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether the given Collection contains the given element instance.
     * <p>
     * Enforces the given instance to be present, rather than returning
     * {@code true} for an equal element as well.
     * 
     * @param collection
     *            the Collection to check
     * @param element
     *            the element to look for
     * @return {@code true} if found, {@code false} else
     */
    public static boolean containsInstance(Collection<?> collection, Object element) {
        if (collection != null) {
            for (Object candidate : collection) {
                if (candidate == element) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return the first element in '{@code candidates}' that is contained in
     * '{@code source}'. If no element in '{@code candidates}' is present in
     * '{@code source}' returns {@code null}. Iteration order is
     * {@link Collection} implementation specific.
     * 
     * @param source
     *            the source Collection
     * @param candidates
     *            the candidates to search for
     * @return the first present object, or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    public static <E> E findFirstMatch(Collection<?> source, Collection<E> candidates) {
        if (isEmpty(source) || isEmpty(candidates)) {
            return null;
        }
        for (Object candidate : candidates) {
            if (source.contains(candidate)) {
                return (E) candidate;
            }
        }
        return null;
    }

    /**
     * Find a single value of the given type in the given Collection.
     * 
     * @param collection
     *            the Collection to search
     * @param type
     *            the type to look for
     * @return a value of the given type found if there is a clear match, or
     *         {@code null} if none or more than one such value found
     */
    @SuppressWarnings("unchecked")
    public static <T> T findValueOfType(Collection<?> collection, Class<T> type) {
        if (isEmpty(collection)) {
            return null;
        }
        T value = null;
        for (Object element : collection) {
            if (type == null || type.isInstance(element)) {
                if (value != null) {
                    // More than one value found... no clear single value.
                    return null;
                }
                value = (T) element;
            }
        }
        return value;
    }

    /**
     * Find a single value of one of the given types in the given Collection:
     * searching the Collection for a value of the first type, then searching
     * for a value of the second type, etc.
     * 
     * @param collection
     *            the collection to search
     * @param types
     *            the types to look for, in prioritized order
     * @return a value of one of the given types found if there is a clear
     *         match, or {@code null} if none or more than one such value found
     */
    public static Object findValueOfType(Collection<?> collection, Class<?>[] types) {
        if (isEmpty(collection) || ObjectUtils2.isEmpty(types)) {
            return null;
        }
        for (Class<?> type : types) {
            Object value = findValueOfType(collection, type);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Determine whether the given Collection only contains a single unique
     * object.
     * 
     * @param collection
     *            the Collection to check
     * @return {@code true} if the collection contains a single reference or
     *         multiple references to the same instance, {@code false} else
     */
    public static boolean hasUniqueObject(Collection<?> collection) {
        if (isEmpty(collection)) {
            return false;
        }
        boolean hasCandidate = false;
        Object candidate = null;
        for (Object elem : collection) {
            if (!hasCandidate) {
                hasCandidate = true;
                candidate = elem;
            } else if (candidate != elem) {
                return false;
            }
        }
        return true;
    }

    /**
     * Find the common element type of the given Collection, if any.
     * 
     * @param collection
     *            the Collection to check
     * @return the common element type, or {@code null} if no clear common type
     *         has been found (or the collection was empty)
     */
    public static Class<?> findCommonElementType(Collection<?> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        Class<?> candidate = null;
        for (Object val : collection) {
            if (val != null) {
                if (candidate == null) {
                    candidate = val.getClass();
                } else if (candidate != val.getClass()) {
                    return null;
                }
            }
        }
        return candidate;
    }

    /**
     * Marshal the elements from the given enumeration into an array of the
     * given type. Enumeration elements must be assignable to the type of the
     * given array. The array returned will be a different instance than the
     * array given.
     */
    public static <A, E extends A> A[] toArray(Enumeration<E> enumeration, A[] array) {
        ArrayList<A> elements = new ArrayList<A>();
        while (enumeration.hasMoreElements()) {
            elements.add(enumeration.nextElement());
        }
        return elements.toArray(array);
    }

    /**
     * Adapt a {@code Map<K, List<V>>} to an {@code MultiValueMap<K, V>}.
     * 
     * @param map
     *            the original map
     * @return the multi-value map
     * @since 3.1
     */
    public static <K, V> MultiValueMap<K, V> toMultiValueMap(Map<K, List<V>> map) {
        return new MultiValueMapAdapter<>(map);
    }

    /**
     * Return an unmodifiable view of the specified multi-value map.
     * 
     * @param map
     *            the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified multi-value map.
     * @since 3.1
     */
    @SuppressWarnings("unchecked")
    public static <K, V> MultiValueMap<K, V> unmodifiableMultiValueMap(MultiValueMap<? extends K, ? extends V> map) {
        Assert2.notNull(map, "'map' must not be null");
        Map<K, List<V>> result = new LinkedHashMap<>(map.size());
        map.forEach((key, value) -> {
            List<? extends V> values = Collections.unmodifiableList(value);
            result.put(key, (List<V>) values);
        });
        Map<K, List<V>> unmodifiableMap = Collections.unmodifiableMap(result);
        return toMultiValueMap(unmodifiableMap);
    }

    /**
     * Adapts a Map to the MultiValueMap contract.
     */
    @SuppressWarnings("serial")
    private static class MultiValueMapAdapter<K, V> implements MultiValueMap<K, V>, Serializable {

        private final Map<K, List<V>> map;

        public MultiValueMapAdapter(Map<K, List<V>> map) {
            Assert2.notNull(map, "'map' must not be null");
            this.map = map;
        }

        @Override
        @Nullable
        public V getFirst(K key) {
            List<V> values = this.map.get(key);
            return (values != null ? values.get(0) : null);
        }

        @Override
        public void add(K key, @Nullable V value) {
            List<V> values = this.map.computeIfAbsent(key, k -> new LinkedList<>());
            values.add(value);
        }

        @Override
        public void addAll(K key, List<? extends V> values) {
            List<V> currentValues = this.map.computeIfAbsent(key, k -> new LinkedList<>());
            currentValues.addAll(values);
        }

        @Override
        public void addAll(MultiValueMap<K, V> values) {
            for (Entry<K, List<V>> entry : values.entrySet()) {
                addAll(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public void set(K key, @Nullable V value) {
            List<V> values = new LinkedList<>();
            values.add(value);
            this.map.put(key, values);
        }

        @Override
        public void setAll(Map<K, V> values) {
            values.forEach(this::set);
        }

        @Override
        public Map<K, V> toSingleValueMap() {
            LinkedHashMap<K, V> singleValueMap = new LinkedHashMap<>(this.map.size());
            this.map.forEach((key, value) -> singleValueMap.put(key, value.get(0)));
            return singleValueMap;
        }

        @Override
        public int size() {
            return this.map.size();
        }

        @Override
        public boolean isEmpty() {
            return this.map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return this.map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return this.map.containsValue(value);
        }

        @Override
        public List<V> get(Object key) {
            return this.map.get(key);
        }

        @Override
        public List<V> put(K key, List<V> value) {
            return this.map.put(key, value);
        }

        @Override
        public List<V> remove(Object key) {
            return this.map.remove(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends List<V>> map) {
            this.map.putAll(map);
        }

        @Override
        public void clear() {
            this.map.clear();
        }

        @Override
        public Set<K> keySet() {
            return this.map.keySet();
        }

        @Override
        public Collection<List<V>> values() {
            return this.map.values();
        }

        @Override
        public Set<Entry<K, List<V>>> entrySet() {
            return this.map.entrySet();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            return this.map.equals(other);
        }

        @Override
        public int hashCode() {
            return this.map.hashCode();
        }

        @Override
        public String toString() {
            return this.map.toString();
        }
    }

    // ----------------------------------------------
    // --- Customization extensions util methods. ---
    // ----------------------------------------------

    /**
     * Is empty array.
     * 
     * @param collection
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean isEmptyArray(T... array) {
        return isNull(array) || array.length <= 0;
    }

    /**
     * Safe collection list.
     * 
     * @param collection
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] safeArray(Class<T> componentType, T... array) {
        return isNull(array) ? (T[]) Array.newInstance(componentType, 0) : array;
    }

    /**
     * Ensure that the default is at least an ArrayList instance (when the
     * parameter is empty)
     * 
     * @param array
     * @return
     */
    public static <T> List<T> safeArrayToList(T[] array) {
        if (isNull(array)) {
            return new ArrayList<>(2);
        }
        List<T> list = new ArrayList<>(array.length);
        for (T t : array)
            list.add(t);
        return list;
    }

    /**
     * Ensure that the default is at least an ArrayList instance (when the
     * parameter is empty)
     * 
     * @param array
     * @return
     */
    public static <T> Set<T> safeArrayToSet(T[] array) {
        return isNull(array) ? new HashSet<>(2) : new HashSet<>(asList(array));
    }

    /**
     * Safe enumeration to list.
     * 
     * @param enum
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> safeEnumerationToList(Enumeration<T> enumeration) {
        return isNull(enumeration) ? emptyList() : EnumerationUtils.toList(enumeration);
    }

    /**
     * Safe collection list.
     * 
     * @param list
     * @return
     */
    public static <T> List<T> safeList(List<T> list) {
        return isEmpty(list) ? emptyList() : list;
    }

    /**
     * Safe collection list.
     * 
     * @param list
     * @return
     */
    public static <T> List<T> safeList(Collection<T> list) {
        return isEmpty(list) ? emptyList() : list.stream().collect(toList());
    }

    /**
     * Safe array to list.
     * 
     * @param array
     * @return
     */
    public static <T> List<T> safeToList(Class<T> componentType, T[] array) {
        return safeArrayToList(safeArray(componentType, array));
    }

    /**
     * Safe collection set.
     * 
     * @param set
     * @return
     */
    public static <T> Set<T> safeSet(Set<T> set) {
        return isEmpty(set) ? emptySet() : set;
    }

    /**
     * Safe collection map.
     * 
     * @param map
     * @return
     */
    public static <K, V> Map<K, V> safeMap(Map<K, V> map) {
        return CollectionUtils2.isEmpty(map) ? emptyMap() : map;
    }

    /**
     * Ensure that the default is at least an ArrayList instance (when the
     * parameter is empty)
     * 
     * @param list
     * @return
     */
    public static <T> List<T> ensureList(List<T> list) {
        return isEmpty(list) ? new ArrayList<T>() : list;
    }

    /**
     * Ensure that the default is at least an fallback list instance (when the
     * parameter is empty)
     * 
     * @param list
     * @param fallback
     * @return
     */
    public static <T> List<T> ensureList(List<T> list, List<T> fallback) {
        return isEmpty(list) ? fallback : list;
    }

    /**
     * Ensure that the default is at least an HashSet instance (when the
     * parameter is empty)
     * 
     * @param set
     * @return
     */
    public static <T> Set<T> ensureSet(Set<T> set) {
        return isEmpty(set) ? new HashSet<T>() : set;
    }

    /**
     * Ensure that the default is at least an fallback set instance (when the
     * parameter is empty)
     * 
     * @param set
     * @param fallback
     * @return
     */
    public static <T> Set<T> ensureSet(Set<T> set, Set<T> fallback) {
        return isEmpty(set) ? fallback : set;
    }

    /**
     * Ensure that the default is at least an HashMap instance (when the
     * parameter is empty)
     * 
     * @param map
     * @return
     */
    public static <K, V> Map<K, V> ensureMap(Map<K, V> map) {
        return isEmpty(map) ? new HashMap<>() : map;
    }

    /**
     * Ensure that the default is at least an fallback map instance (when the
     * parameter is empty)
     * 
     * @param map
     * @param fallback
     * @return
     */
    public static <K, V> Map<K, V> ensureMap(Map<K, V> map, Map<K, V> fallback) {
        return isEmpty(map) ? fallback : map;
    }

    /**
     * Remove duplicate collection elements.
     * 
     * @param collection
     * @return
     */
    public static <T> Collection<T> disDupCollection(Collection<T> collection) {
        Set<T> disSet = new HashSet<>(collection);
        collection.clear();
        collection.addAll(disSet);
        return collection;
    }

    /**
     * Extract iterable element by iterations(index).
     * 
     * @param iter
     * @param defaultValue
     * @return
     */
    @Nullable
    public static <T> T extractFirst(@Nullable Iterable<T> iter) {
        return extractElement(iter, 0, null);
    }

    /**
     * Extract iterable element by iterations(index).
     * 
     * @param iter
     * @param defaultValue
     * @return
     */
    @Nullable
    public static <T> T extractFirst(@Nullable Iterable<T> iter, @Nullable T defaultValue) {
        return extractElement(iter, 0, defaultValue);
    }

    /**
     * Extract iterable element by iterations(index).
     * 
     * @param iter
     *            Target collection iterable
     * @param byIndex
     *            The element to be extracted belongs to that iteration
     * @param defaultValue
     *            Default value.
     * @return
     */
    @Nullable
    public static <T> T extractElement(@Nullable Iterable<T> iter, int byIndex, @Nullable T defaultValue) {
        isTrue(byIndex >= 0, format("byIterations(%s) must >= 0", byIndex));
        if (isNull(iter)) {
            return defaultValue;
        }

        int i = 0;
        Iterator<T> it = iter.iterator();
        while (it.hasNext()) {
            T t = it.next();
            if (byIndex == i++) {
                return t;
            }
        }

        return defaultValue;
    }

    /**
     * Move the first matching element to the first position.
     * 
     * @return
     */
    @Nullable
    public static <T> void toFirstElement(@Nullable List<T> list, @NotNull ProcessFunction<T, Boolean> processor) {
        notNullOf(processor, "comparator");
        if (isNull(list)) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            try {
                T curr = list.get(i);
                if (processor.process(curr)) {
                    T oldFirst = list.get(0);
                    list.set(0, curr);
                    list.set(i, oldFirst);
                    break;
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

}