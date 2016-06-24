package org.togglz.appengine.repository;

import com.google.appengine.api.datastore.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Non final implementation of DatastoreService to allow Mockito.Spy
 */
public class DelegateDatastoreService implements DatastoreService {

    private DatastoreService delegate;

    private DelegateDatastoreService(DatastoreService delegate) {
        this.delegate = delegate;
    }

    static DelegateDatastoreService getInstance(DatastoreService delegate) {
        return new DelegateDatastoreService(delegate);
    }

    public Entity get(Key key) throws EntityNotFoundException {
        return delegate.get(key);
    }

    public PreparedQuery prepare(Transaction transaction, Query query) {
        return delegate.prepare(transaction, query);
    }

    public Transaction getCurrentTransaction() {
        return delegate.getCurrentTransaction();
    }

    public PreparedQuery prepare(Query query) {
        return delegate.prepare(query);
    }

    public KeyRange allocateIds(String s, long l) {
        return delegate.allocateIds(s, l);
    }

    public Collection<Transaction> getActiveTransactions() {
        return delegate.getActiveTransactions();
    }

    public Entity get(Transaction transaction, Key key) throws EntityNotFoundException {
        return delegate.get(transaction, key);
    }

    public Map<Key, Entity> get(Transaction transaction, Iterable<Key> iterable) {
        return delegate.get(transaction, iterable);
    }

    public Map<Key, Entity> get(Iterable<Key> iterable) {
        return delegate.get(iterable);
    }

    public Key put(Transaction transaction, Entity entity) {
        return delegate.put(transaction, entity);
    }

    public void delete(Transaction transaction, Iterable<Key> iterable) {
        delegate.delete(transaction, iterable);
    }

    public Map<Index, Index.IndexState> getIndexes() {
        return delegate.getIndexes();
    }

    public DatastoreAttributes getDatastoreAttributes() {
        return delegate.getDatastoreAttributes();
    }

    public Transaction beginTransaction(TransactionOptions transactionOptions) {
        return delegate.beginTransaction(transactionOptions);
    }

    public Key put(Entity entity) {
        return delegate.put(entity);
    }

    public void delete(Iterable<Key> iterable) {
        delegate.delete(iterable);
    }

    public Transaction getCurrentTransaction(Transaction transaction) {
        return delegate.getCurrentTransaction(transaction);
    }

    public DatastoreService.KeyRangeState allocateIdRange(KeyRange keyRange) {
        return delegate.allocateIdRange(keyRange);
    }

    public void delete(Key... keys) {
        delegate.delete(keys);
    }

    public KeyRange allocateIds(Key key, String s, long l) {
        return delegate.allocateIds(key, s, l);
    }

    public List<Key> put(Iterable<Entity> iterable) {
        return delegate.put(iterable);
    }

    public List<Key> put(Transaction transaction, Iterable<Entity> iterable) {
        return delegate.put(transaction, iterable);
    }

    public Transaction beginTransaction() {
        return delegate.beginTransaction();
    }

    public void delete(Transaction transaction, Key... keys) {
        delegate.delete(transaction, keys);
    }
}
