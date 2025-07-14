package com.morpheus.grails.elasticsearch

import groovy.json.JsonOutput


/**
* Simplified ElasticSearch Query Builder for performing Query Operations
*
* @author Brian Wheeler
*/
class ElasticQueryBuilder {

	//query calls
	static RootQuery rootQuery() {
		return new RootQuery()
	}

	static RootQuery rootQuery(String index, String type) {
		return new RootQuery(index, type)
	}

	static CountQuery countQuery() {
		return new CountQuery()
	}

	static CountQuery countQuery(String index, String type) {
		return new CountQuery(index, type)
	}

	static BoolQuery boolQuery() {
		return new BoolQuery()
	}

	static TermQuery termQuery(String field, Object value) {
		return new TermQuery(field, value)
	}

	static TermsQuery termsQuery(String field, List values) {
		return new TermsQuery(field, values)
	}

	static TermsQuery termsQuery(String field, Object[] values) {
		return new TermsQuery(field, Arrays.asList(values))
	}

	static RangeQuery rangeQuery(String field) {
		return new RangeQuery(field)
	}

	static TypeQuery typeQuery(String type) {
		return new TypeQuery(type)
	}

	static QueryStringQuery queryStringQuery(String queryString) {
		return new QueryStringQuery(queryString)
	}

	static SimpleQueryStringQuery simpleQueryStringQuery(String queryString) {
		return new SimpleQueryStringQuery(queryString)
	}

	static SimpleQueryStringQuery simpleQueryStringQuery(String queryString, String defaultField) {
		return new SimpleQueryStringQuery(queryString, defaultField)
	}

	static QueryStringQuery queryStringQuery(String queryString, String defaultField) {
		return new QueryStringQuery(queryString, defaultField)
	}

	static IdsQuery idsQuery() {
		return new IdsQuery()
	}

	static NestedQuery nestedQuery(String path, Object query, String scoreMode) {
		return new NestedQuery(path, query, scoreMode)
	}

	static PrefixQuery prefixQuery(String field, Object value) {
		return new PrefixQuery(field, value)
	}

	static MatchQuery matchQuery(String field, Object value) {
		return new MatchQuery(field, value)
	}

	static MatchAllQuery matchAllQuery() {
		return new MatchAllQuery()
	}

	static MatchPhrasePrefixQuery matchPhrasePrefixQuery(String field, Object value) {
		return new MatchPhrasePrefixQuery(field, value)
	}

	static ExistsQuery existsQuery(String field) {
		return new ExistsQuery(field)
	}

	static MultiSearch prepareMultiSearch() {
		return new MultiSearch()
	}

	static BulkRequest prepareBulk() {
		return new BulkRequest()
	}

	//

	//internal classes
	static class BaseQuery {

		Map body = [:]
		Map queryTarget

		String toString() {
			return JsonOutput.toJson(body)
		}

		def setBaseKeyValue(String key, Object value) {
			body[key] = value
			return this
		}

		def setKeyValue(String key, Object value) {
			queryTarget[key] = value
			return this	
		}

		Map getQuery() {
			//println("getQuery: ${queryTarget}")
			return queryTarget
		}

		Map getMultiQuery() {
			return body
		}

	}

	//root query - use it like the elastic client prepare search
	static class RootQuery extends BaseQuery {

		String index
		String type

		public RootQuery() {
			body = [from:0, size:10, query:[:]]
			queryTarget = body.query
		}

		public RootQuery(String index, String type) {
			this.index = index
			this.type = type
			body = [from:0, size:10, query:[:]]
			queryTarget = body.query
		}

		RootQuery setSize(Integer size) {
			body.size = size
			return this
		}

		RootQuery setFrom(Integer from) {
			body.from = from
			return this
		}
		
		RootQuery prepareDelete() {
			body.remove('from')
			body.remove('size')
			return this
		}

		RootQuery addSort(String field, String order) {
			body.sort = body.sort ?: []
			def newSort = [:]
			newSort[field] = [order:order?.toLowerCase()]
			body.sort << newSort
			return this
		}

		RootQuery addSort(String field, String order, String unmappedType, String missing) {
			body.sort = body.sort ?: []
			def newSort = [:]
			newSort[field] = [order:order?.toLowerCase()]
			if(unmappedType)
				newSort[field].unmapped_type = unmappedType
			if(missing)
				newSort[field].missing = missing
			body.sort << newSort
			return this
		}

		RootQuery addIndexBoost(String index, Float boost) {
			body.indices_boost = body.indices_boost ?: []
			def newBoost = [:]
			newBoost[index] = boost
			body.indices_boost << newBoost
			return this  
		}

		RootQuery setQuery(query) {
			body.query = query.body
			queryTarget = body.query
			return this
		}

		RootQuery addAggregation(Object aggregation) {
			body.aggs = body.aggs ?: [:]
			body.aggs << aggregation.body
			return this
		}

		RootQuery addAggregation(String name, Object aggregation) {
			body.aggs = body.aggs ?: [:]
			body.aggs[name] = aggregation.body
			return this
		}

		Map getMultiQuery() {
			return body
		}

	}

	static class CountQuery extends BaseQuery {

		String index
		String type

		public CountQuery() {
			body = [from:0, size:0, query:[:]]
			queryTarget = body.query
		}

		public CountQuery(String index, String type) {
			this.index = index
			this.type = type
			body = [from:0, size:0, query:[:]]
			queryTarget = body.query
		}

		void setSize(Integer size) {
			body.size = size
			return this
		}

		void setFrom(Integer from) {
			body.from = from
			return this
		}

		void addSort(String field, String order) {
			body.sort = body.sort ?: []
			def newSort = [:]
			newSort[field] = [order:order?.toLowerCase()]
			body.sort << newSort
			return this
		}

		void setQuery(query) {
			body.query = query.body
			queryTarget = body.query
			return this
		}

		Map getMultiQuery() {
			return body
		}

	}

	static class TypeQuery extends BaseQuery {
		public TypeQuery(String type) {
			body.type = [:]
			body.type.value = type
			queryTarget = body.type
		}
	}

	static class ExistsQuery extends BaseQuery {
		public ExistsQuery(String field) {
			body.exists = [field:field]
			queryTarget = body.exists
		}
	}

	static class TermQuery extends BaseQuery {
		public TermQuery(String field, Object value) {
			body.term = [:]
			body.term[field] = value
			queryTarget = body.term
		}
	}

	static class TermsQuery extends BaseQuery {
		public TermsQuery(String field, List values) {
			body.terms = [:]
			body.terms[field] = values
			queryTarget = body.terms
		}
	}

	static class PrefixQuery extends BaseQuery {
		public PrefixQuery(String field, Object value) {
			body.prefix = [:]
			body.prefix[field] = value
			queryTarget = body.prefix
		}
	}

	static class MatchQuery extends BaseQuery {
		public MatchQuery(String field, Object value) {
			body.match = [:]
			body.match[field] = value
			queryTarget = body.match
		}
	}

	static class MatchAllQuery extends BaseQuery {
		public MatchAllQuery() {
			body.match_all = [:]
			queryTarget = body.match_all
		}
	}

	static class MatchPhrasePrefixQuery extends BaseQuery {
		public MatchPhrasePrefixQuery(String field, Object value) {
			body.match_phrase_prefix = [:]
			body.match_phrase_prefix[field] = value
			queryTarget = body.match_phrase_prefix
		}
	}

	static class IdsQuery extends BaseQuery {
		public IdsQuery() {
			body.ids = [values:[]]
			queryTarget = body.ids
		}

		IdsQuery setTypes(String type) {
			queryTarget.type = type
			return this
		}

		IdsQuery addIds(List idList) {
			queryTarget.values += idList
			return this
		}

		IdsQuery addIds(Object object) {
			queryTarget.values << object
			return this
		}
	}

	static class NestedQuery extends BaseQuery {
		public NestedQuery(String path, Object query, String scoreMode) {
			body.nested = [
				path:path,
				score_mode:scoreMode,
				query:query.body
			]
			queryTarget = body.nested
		}
	}

	static class QueryStringQuery extends BaseQuery {

		public QueryStringQuery(String queryString) {
			body.query_string = [:]
			body.query_string.query = queryString
			queryTarget = body.query_string
		}

		public QueryStringQuery(String queryString, String defaultField) {
			body.query_string = [:]
			body.query_string.query = queryString
			body.query_string.default_field = defaultField
			queryTarget = body.query_string
		}
		
		QueryStringQuery defaultOperator(String value) {
			body.query_string.default_operator = value
			return this
		}
		
		QueryStringQuery lenient(Boolean value) {
			body.query_string.lenient = value
			return this
		}
		
		QueryStringQuery analyzer(String value) {
			body.query_string.analyzer = value
			return this
		}
		
		QueryStringQuery autoGeneratePhraseQueries(Boolean value) {
			//body.query_string.auto_generate_phrase_queries = value
			return this
		}

		QueryStringQuery fields(Collection values) {
			body.query_string.fields = values
			return this
		}
		
		QueryStringQuery type(String value) {
			body.query_string.type = value
			return this
		}

		QueryStringQuery rewrite(String value) {
			body.query_string.rewrite = value
			return this
		}

		QueryStringQuery phraseSlop(Integer value) {
			body.query_string.phrase_slop = value
			return this
		}
		
		QueryStringQuery analyzeWildcard(Boolean value) {
			body.query_string.analyze_wildcard = value
			return this
		}
	}

	static class SimpleQueryStringQuery extends BaseQuery {

		public SimpleQueryStringQuery(String queryString) {
			body.simple_query_string = [:]
			body.simple_query_string.query = queryString
			queryTarget = body.simple_query_string
		}

		public SimpleQueryStringQuery(String queryString, String defaultField) {
			body.simple_query_string = [:]
			body.simple_query_string.query = queryString
			body.simple_query_string.default_field = defaultField
			queryTarget = body.simple_query_string
		}
		
		SimpleQueryStringQuery defaultOperator(String value) {
			body.simple_query_string.default_operator = value
			return this
		}
		
		SimpleQueryStringQuery lenient(Boolean value) {
			body.simple_query_string.lenient = value
			return this
		}
		
		SimpleQueryStringQuery analyzer(String value) {
			body.simple_query_string.analyzer = value
			return this
		}
		
		SimpleQueryStringQuery autoGenerateSynonymsPhraseQueries(Boolean value) {
			body.simple_query_string.auto_generate_synonyms_phrase_query = value
			return this
		}

		SimpleQueryStringQuery fields(Collection values) {
			body.simple_query_string.fields = values
			return this
		}
		
		
		
		
		
	}

	static class BoolQuery extends BaseQuery {

		public BoolQuery() {
			body.bool = [:]
			queryTarget = body.bool
		}

		BoolQuery must(query) {
			queryTarget.must = addFlexArrayMapValue(queryTarget.must, query.body)
			return this
		}

		BoolQuery should(query) {
			queryTarget.should = addFlexArrayMapValue(queryTarget.should, query.body)
			return this
		}

		BoolQuery filter(query) {
			queryTarget.filter = addFlexArrayMapValue(queryTarget.filter, query.body)
			return this	
		}

		BoolQuery mustNot(query) {
			queryTarget.must_not = addFlexArrayMapValue(queryTarget.must_not, query.body)
			return this
		}

	}

	static class RangeQuery extends BaseQuery {
		
		String field
		
		public RangeQuery(String field) {
			this.field = field
			body.range = [:]
			body.range[field] = [:]
			queryTarget = body.range[field]
		}

		RangeQuery from(Object value) {
			queryTarget.gte = value
			return this
		}

		RangeQuery to(Object value) {
			queryTarget.lte = value
			return this
		}

		RangeQuery gte(Object value) {
			queryTarget.gte = value
			return this
		}

		RangeQuery lte(Object value) {
			queryTarget.lte = value
			return this
		}

		RangeQuery gt(Object value) {
			queryTarget.gt = value
			return this
		}

		RangeQuery lt(Object value) {
			queryTarget.lt = value
			return this
		}

		void boost(Double value) {
			queryTarget.boost = value
			return this
		}

	}

	static class MultiSearch {
		
		List searchItems

		public MultiSearch() {
			searchItems = []
		}

		void add(String index, String type, String id, Object item) {
			def headerRow = ['index':index ?: '_all']
			if(type)
				headerRow.type = type
			if(id)
				headerRow.id = id
			searchItems << headerRow
			searchItems << item.getMultiQuery()
		}

		Boolean hasContent() {
			return searchItems.size() > 0
		}

		def getRequestBody() {
			def rtn = searchItems.collect{ multiItem ->
				return JsonOutput.toJson(multiItem)
			}?.join('\n')
			return rtn
		}

	}

	static class BulkRequest {

		List bulkItems

		public BulkRequest() {
			bulkItems = []
		}

		void add(String action, String index, String type, Object id, Map document) {
			def actionRow = [:]
			actionRow[action] = ['_index':index, '_id':id]
			bulkItems << actionRow
			if(document)
				bulkItems << document
		}

		Boolean hasContent() {
			return bulkItems?.size() > 0
		}

		def getRequestBody() {
			def rtn = bulkItems.collect{ bulkItem ->
				return JsonOutput.toJson(bulkItem)
			}?.join('\n') + '\n'
			return rtn
		}

	}

	//internal stuff
	static addFlexArrayMapValue(Object target, Map addon) {
		def rtn
		if(target) {
			if(target instanceof List) {
				target << addon
				rtn = target
			} else if(target instanceof Map) {
				rtn = []
				rtn << target
				rtn << addon
			}
		} else {
			rtn = addon
		}
		return rtn
	}

}
