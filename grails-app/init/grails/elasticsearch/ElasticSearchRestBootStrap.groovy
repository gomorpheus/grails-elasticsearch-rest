package grails.elasticsearch

class BootStrap {
	def elasticAdminService
    def init = { servletContext ->
    	elasticAdminService.applicationStartup()
    }
    def destroy = {
    }
}
