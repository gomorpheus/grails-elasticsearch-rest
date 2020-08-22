Grails REST ElasticSearch
=========================

This plugin provides similar functionality to the Official Grails ElasticSearch Plugin, but instead utilizes the low level REST Client for better efficiency and reduced transitive dependencies. The Official Grails ElasticSearch Plugin can be found [here](https://plugins.grails.org/plugin/puneetbehl/elasticsearch). This plugin offers similar functionality in the following areas:

* Indexable Domain Classes (via the searchable Closure definition)
* Low Level Calls to Elastic via the ElasticService
* Index on Startup Configuration
* GrailsSearchableCompileStatic annotation to allow static compilation of Domain Classes using the `searchable` definition

Configuration
-------------

Add The `grails-elasticsearch-client` to your `build.gradle`. 

Configuration options can be specified in your `application.yml`


```yml
elasticSearch:
    bulkIndexOnStartup: false
    protocol: http
    user: myUsername
    password: myPassword
    cluster:
        name: myclustername
    client:
        hosts:
            - {host: 127.0.0.1, port: 9200}
    index:
        numberOfReplicas: 1
```


Usage
-----

There are multiple ways to utilize this plugin. One way is to use it to index all GORM Domain Classes. This can be done by defing the `searchable` attributes on the Domain Class.


```groovy
	static searchable = {
		setIndexName('morpheus_my_book')
		root true

		//set field types
		description type:'keyword'
		internalId type:'text', fields:[raw:[type:'keyword']]

		//references
		account reference:true

		//exclude fields
		setExcept(['config'])
		
	}
```

For a list of Available types please refer to the ElasticSearch official documentation.

In addition to providing a means to defining an index for a `DomainClass`, This plugin provides a way to statically compile your Classes via the `GrailsSearchableCompileStatic` Annotation.

```groovy
import morpheus.grails.compiler.GrailsSearchableCompileStatic

@GrailsSearchableCompileStatic
class MyDomain {
	static searchable = {

	}

	String name

	...
}
```


Please refer to the javadoc on examples of how to use the `ElasticService` as well as the `ElasticQueryBuilder`

Below is an example of performing a search operation

```groovy
	def executeSearch(Account account, User user, Map opts) {
		def rtn = [success:false, hits:[]]
		def max = opts.max ? opts.max.toInteger() : 25
		if(opts.max == 0)
			max = 0
		def offset = opts.offset ? opts.offset.toInteger() : 0
		def requestedIndexes = [getSearchIndexForCategory(opts.category)]
		def searchScope = getSearchIndexScopeForUser(account, user, requestedIndexes)
		if(searchScope) {
			def siteIds = permissionService.getListOfSitesUserCanView(user) 
			def zoneIds = permissionService.getListOfZonesAccountCanSee(user.account)
			def indexName = searchScope?.join(',')
			//build query
			def rootQuery = ElasticQueryBuilder.rootQuery(indexName, '')
			//paging
			rootQuery.setFrom(offset)
			if(max)
				rootQuery.setSize(max)
			//set boosts
			searchScope?.each { idx ->
				rootQuery.addIndexBoost(idx, INDEX_BOOSTS[idx] ?: 1.0f)
			}
			//build filters
			def boolQuery = ElasticQueryBuilder.boolQuery()
			//add scope filtering
			def scopeQuery = buildScopeQuery(account, user, siteIds, zoneIds, opts)
			boolQuery.filter(scopeQuery)
			//search
			if(opts.query) {
				def searchPhrase = opts.query
				//add wildcard?
				if(!searchPhrase.contains('-') && !searchPhrase.contains('*') && !searchPhrase.contains(':'))
					searchPhrase += '*'
				//setup phrase query
				def phraseQuery = ElasticQueryBuilder.queryStringQuery(searchPhrase.toLowerCase())
				phraseQuery.defaultOperator('AND')
				phraseQuery.lenient(true)
				//phraseQuery.type('bool_prefix')
				//set fields
				def searchFields = ['name^1.5', 'displayName^2', 'externalId^1.5', 'description', 'code^0.1', 'category^0.1', 
					'externalIp', 'internalIp', 'searchName^2', '_all^0.75']
				phraseQuery.fields(searchFields)
				boolQuery.must(phraseQuery)
			}
			//time range
			if(opts.startDate && opts.endDate) {
				def startDate = Date.parse("yyyy-MM-dd HH:mm:ss", opts.startDate)
				def endDate = Date.parse("yyyy-MM-dd HH:mm:ss", opts.endDate)
				def createdDateQuery = ElasticQueryBuilder.rangeQuery('dateCreated')
					.gte(startDate.time)
					.lte(endDate.time)
				boolQuery.must(createdDateQuery)
			}
			//sort
			if(opts.sort)
				rootQuery.addSort(opts.sort, opts.direction == 'asc' ? 'asc' : 'desc', 'date', '_last')
			else
				rootQuery.addSort("_score", 'desc')
			//execute it
			rootQuery.setQuery(boolQuery)
			def searchOpts = [queryType:'dfs_query_then_fetch']
			def results = elasticService.executeSearch(rootQuery, searchOpts)
			//check results
			if(results.success == true) {
				rtn.total = results.hits?.total?.value ?: 0
				results.hits?.hits?.each { hit ->
					def row = hit['_source']
					row._index = ElasticAdminService.versionToIndex(hit['_index'])
					row._id = hit['_id']
					row._score = hit['_score']
					row._type = row._index //hit['_type']
					rtn.hits << row
				}
				rtn.took = results.took
				rtn.timedOut = results.timedOut
				rtn.success = true
			}
		} else {
			//nothing
		}
		return rtn
	}
```


**TODO: Add More Documentation**