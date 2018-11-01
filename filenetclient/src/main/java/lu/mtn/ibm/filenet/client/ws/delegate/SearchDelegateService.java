/**
 *
 */
package lu.mtn.ibm.filenet.client.ws.delegate;

import java.util.Iterator;
import java.util.List;

import com.filenet.ns.fnce._2006._11.ws.mtom.wsdl.FNCEWS40PortType;
import com.filenet.ns.fnce._2006._11.ws.mtom.wsdl.FaultResponse;
import com.filenet.ns.fnce._2006._11.ws.schema.EndOfCollection;
import com.filenet.ns.fnce._2006._11.ws.schema.EndOfPage;
import com.filenet.ns.fnce._2006._11.ws.schema.Localization;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectFactory;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectSetType;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectStoreScope;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectValue;
import com.filenet.ns.fnce._2006._11.ws.schema.RepositorySearch;
import com.filenet.ns.fnce._2006._11.ws.schema.RepositorySearchModeType;

/**
 * @author NguyenT
 *
 */
public class SearchDelegateService extends AbstractDelegateService {

    /**
     * @param port
     * @param defaultLocale
     * @param factory
     */
    public SearchDelegateService(FNCEWS40PortType port, Localization defaultLocale, ObjectFactory factory) {
        super(port, defaultLocale, factory);
    }

    /**
     * @param objectStoreName The objectStore name.
     * @param query The query to execute.
     * @param pagingSize The paging size.
     * @return A search iterator.
     * @throws FaultResponse if occurs.
     */
    public Iterator<ObjectValue> search(String objectStoreName, String query, int pagingSize) throws FaultResponse {
        RepositorySearch request = createSearchRequest(objectStoreName, query, pagingSize, RepositorySearchModeType.OBJECTS);

        ObjectSetType result = port.executeSearch(request, defaultLocale);
        return new SearchResultIterator(port, result, request, defaultLocale);
    }

    /**
     * @param objectStoreName
     * @param query
     * @param pagingSize
     * @param continuable
     * @param mode
     * @return
     */
    protected RepositorySearch createSearchRequest(String objectStoreName, String query, Integer pagingSize, RepositorySearchModeType mode) {
        // Specify the scope of the search
        ObjectStoreScope objectStoreScope = factory.createObjectStoreScope();
        objectStoreScope.setObjectStore(objectStoreName);

        // Create RepositorySearch
        RepositorySearch repositorySearch = factory.createRepositorySearch();
        repositorySearch.setRepositorySearchMode(mode);

        if (pagingSize != null) {
            repositorySearch.setMaxElements(pagingSize);
            repositorySearch.setContinuable(true);
        }
        repositorySearch.setSearchScope(objectStoreScope);

        // Search for documents matching this DocumentTitle property value
        repositorySearch.setSearchSQL(query);

        return repositorySearch;
    }

    /**
     * @author NguyenT
     *
     */
    protected static class SearchResultIterator implements Iterator<ObjectValue> {

        private FNCEWS40PortType port;

        private boolean hasNext;

        private RepositorySearch repositorySearch;

        private List<ObjectValue> values;

        private Localization defaultLocale;

        public SearchResultIterator(FNCEWS40PortType port, ObjectSetType objectSetType, RepositorySearch repositorySearch, Localization defaultLocale) {
            super();
            this.port = port;
            this.repositorySearch = repositorySearch;
            this.defaultLocale = defaultLocale;

            this.setObjectSetType(objectSetType);
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return hasNext || !values.isEmpty();
        }

        /**
         * @see java.util.Iterator#next()
         */
        @Override
        public ObjectValue next() {

            try {
                // If the list is empty but there are still elements return by the result, continue
                if (values.isEmpty() && hasNext) {
                    ObjectSetType objectSetType = port.executeSearch(repositorySearch, defaultLocale);

                    this.setObjectSetType(objectSetType);
                }

                // Remove and return the first element.
                return this.values.remove(0);

            } catch (FaultResponse e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Could not remove element for this iterator.");
        }

        /**
         * @param objectSetType
         */
        protected void setObjectSetType(ObjectSetType objectSetType) {
            // Check if there are still elements not retrieve yet by the result ie. check if there is a continueFrom value
            if (objectSetType.getCollectionTerminator() instanceof EndOfCollection || ((EndOfPage) objectSetType.getCollectionTerminator()).getContinueFrom() == null) {
                hasNext = false;
            } else {
                hasNext = true;
                this.repositorySearch.setContinueFrom(((EndOfPage) objectSetType.getCollectionTerminator()).getContinueFrom());
            }

            // Retrieve all values in the current paging result.
            this.values = objectSetType.getObject();
        }
    }
}
