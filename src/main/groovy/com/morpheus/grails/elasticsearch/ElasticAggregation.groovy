package com.morpheus.grails.elasticsearch

import groovy.json.JsonOutput

class ElasticAggregation {

	static RootAggregation rootAggregation() {
		return new RootAggregation()
	}

	static RootAggregation rootAggregation(String index, String type) {
		return new RootAggregation(index, type)
	}

	//aggregation calls
	static FilterAggregation filter(String name, Object query) {
		return new FilterAggregation(name, query)
	}

	static DateHistogramAggregation dateHistogram(String name) {
		return new DateHistogramAggregation(name)
	}

	static TermsAggregation terms(String name) {
		return new TermsAggregation(name)
	}

	static MetricAggregation avg(String name) {
		return new MetricAggregation('avg', name)
	}

	static MetricAggregation sum(String name) {
		return new MetricAggregation('sum', name)
	}

	static MetricAggregation max(String name) {
		return new MetricAggregation('max', name)
	}

	static MetricAggregation min(String name) {
		return new MetricAggregation('min', name)
	}

	static MetricAggregation metric(String type, String name) {
		return new MetricAggregation(type, name)
	}

	static MetricAggregation stats(String name) {
		return new MetricAggregation('stats', name)
	}

	static SamplerAggregation sampler(String name) {
		return new SamplerAggregation(name)
	}

	static CategorizeTextAggregation categorizeText(String name) {
		return new CategorizeTextAggregation(name)
	}

	//internal classes
	static class BaseAggregation {

		String name
		Map body = [:]
		Map aggTarget

		def subAggregation(Object aggregation) {
			body[name].aggs = body[name].aggs ?: [:]
			body[name].aggs << aggregation.body
			return this
		}

		def setBaseKeyValue(String key, Object value) {
			body[key] = value
			return this
		}

		def setKeyValue(String key, Object value) {
			aggTarget[key] = value
			return this
		}

		String toString() {
			return JsonOutput.toJson(body)
		}

		Map getAggregation() {
			//println("getAggregation: ${aggTarget}")
			return aggTarget
		}

		Map getMultiAggregation() {
			return body
		}

	}

	static class RootAggregation extends BaseAggregation {

		String index
		String type

		public RootAggregation() {
			body = [aggs:[:]]
			aggTarget = body.aggs
		}

		public RootAggregation(String index, String type) {
			this.index = index
			this.type = type
			body = [aggs:[:]]
			aggTarget = body.aggs
		}

		RootAggregation setSize(Integer size) {
			body.size = size
			return this
		}

		RootAggregation setFrom(Integer from) {
			body.from = from
			return this
		}

		RootAggregation addSort(String field, String order) {
			body.sort = body.sort ?: []
			def newSort = [:]
			newSort[field] = [order:order?.toLowerCase()]
			body.sort << newSort
			return this
		}

		RootAggregation setAggregation(Object agg) {
			body.aggs = agg.body
			return this
		}

	}

	static class FilterAggregation extends BaseAggregation {

		public FilterAggregation(String name, Object query) {
			this.name = name
			body = [:]
			body[name] = [filter:query.getQuery()]
			aggTarget = body[name]
		}

		FilterAggregation setSize(Integer size) {
			body.size = size
			return this
		}

		FilterAggregation setFrom(Integer from) {
			body.from = from
			return this
		}

		FilterAggregation addSort(String field, String order) {
			body.sort = body.sort ?: []
			def newSort = [:]
			newSort[field] = [order:order?.toLowerCase()]
			body.sort << newSort
			return this
		}

		FilterAggregation setQuery(query) {
			body.query = query.body
			println("setQuery: ${query}")
			return this
		}

	}

	static class TermsAggregation extends BaseAggregation {

		public TermsAggregation(String name) {
			this.name = name
			body = [:]
			body[name] = [terms:[:]]
			aggTarget = body[name].terms
		}

		TermsAggregation field(String field) {
			aggTarget.field = field
			return this
		}

		TermsAggregation size(Integer size) {
			aggTarget.size = size
			return this
		}

	}

	static class SamplerAggregation extends BaseAggregation {

		public SamplerAggregation(String name) {
			this.name = name
			body = [:]
			body[name] = [sampler:[:]]
			aggTarget = body[name].sampler
		}

		SamplerAggregation shardSize(Integer size) {
			aggTarget['shard_size'] = size
			return this
		}

	}

	static class CategorizeTextAggregation extends BaseAggregation {

		public CategorizeTextAggregation(String name) {
			this.name = name
			body = [:]
			body[name] = ['categorize_text':[:]]
			aggTarget = body[name]['categorize_text']
		}

		CategorizeTextAggregation field(String field) {
			aggTarget.field = field
			return this
		}

		CategorizeTextAggregation size(Integer size) {
			aggTarget.size = size
			return this
		}

		CategorizeTextAggregation maxMatchedTokens(Integer value) {
			aggTarget['max_matched_tokens'] = value
			return this
		}

		CategorizeTextAggregation similarityThreshold(Integer value) {
			aggTarget['similarity_threshold'] = value
			return this
		}

	}

	//date histogram
	static class DateHistogramAggregation extends BaseAggregation {

		public DateHistogramAggregation(String name) {
			this.name = name
			body = [:]
			body[name] = [date_histogram:[:]]
			aggTarget = body[name].date_histogram
		}

		DateHistogramAggregation field(String field) {
			aggTarget.field = field
			return this
		}

		DateHistogramAggregation minDocCount(Integer minDocCount) {
			aggTarget.min_doc_count = minDocCount
			return this
		}

		DateHistogramAggregation interval(Long interval) {
			aggTarget.fixed_interval = "${interval}ms"
			return this
		}

		DateHistogramAggregation format(String format) {
			aggTarget.format = format
			return this
		}

		DateHistogramAggregation extendedBounds(min, max) {
			aggTarget.extended_bounds = [
				min:min,
				max:max
			]
			return this
		}

	}

	//metrics
	static class MetricAggregation extends BaseAggregation {

		String metricType

		public MetricAggregation(String metricType, String name) {
			this.metricType = metricType
			this.name = name
			body = [:]
			body[name] = [:]
			body[name][metricType] = [:]
			aggTarget = body[name][metricType]
		}

		MetricAggregation field(String field) {
			aggTarget.field = field
			return this
		}

		def setSize(Integer size) {
			aggTarget.size = size
			return this
		}

		def addSort(String field, String order) {
			aggTarget.sort = aggTarget.sort ?: []
			def newSort = [:]
			newSort[field] = [order:order?.toLowerCase()]
			aggTarget.sort << newSort
			return this
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
