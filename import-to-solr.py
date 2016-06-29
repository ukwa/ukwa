import rson
import requests
import configparser
import codecs
import solr

# Notes
# 
# Collections key should not be quoted, i.e. "1" not "\"1\""
# Collections could include the full collections properties
# Targets do not report collection_cats
# The /api/targets/bycollection/X hook appears to be fully recursive.
# The instances are not available via the API (fix checked in)
# Collection.updatedAt appears to be in milliseconds rather than the usual seconds since epoch.

config = configparser.ConfigParser()
config.read('act.cfg')

actUrl = config.get('act', 'url')

# create a connection to a Solr server
solrUrl = config.get('solr', 'url')
print "solrUrl=" + solrUrl
s = solr.SolrConnection(solrUrl)

fullUrl = actUrl + "/login"
print "fullUrl=" + fullUrl

#requests.encoding = 'utf8'

response = requests.post(fullUrl, 
	data={"email": config.get('act', 'username'), 
	"password": config.get('act','password')})
	
if response.status_code != 200:
	print "Web request returned status " + str(response.status_code)
	
else:	
	cookie = response.history[0].headers["set-cookie"]
	headers = {
		"Cookie": cookie
	}

	all = requests.get(actUrl + "/api/collections", headers=headers)
	
	collections_tree = rson.loads(all.content)
	for c in collections_tree:
		c_id = int(c['key'])
		col_url = actUrl + "/api/collections/%s" % c_id
		col_req = requests.get(col_url, headers=headers)
		col = rson.loads(col_req.content.decode('utf8').encode('ascii', 'ignore'))
		if col['field_publish'] == True:
			print("Publishing...",c['title'])
			
			# add a document to the Solr index
			s.add(id=col["id"],
				type="collection",
				name=col["name"],
				description=col["description"]
				)
			
			# Look up all Targets with in this Collection and add them.
			t_url = actUrl + "/api/targets/bycollection/%s" % c_id
			t_req = requests.get(t_url, headers=headers)
			targets = rson.loads(t_req.content)			

			for t in targets:
				target_url = actUrl + "/api/targets/%s" % int(t)
				print "target_url=" + target_url
				target_req = requests.get(target_url, headers=headers)
				target = rson.loads(target_req.content.decode('utf8').encode('ascii', 'ignore'))
				target['collection'] = c_id
															
				# add a document to the Solr index
				s.add(id=target["id"],
					type="target",
					parentId=c_id,
					title=target["title"],
					description=target["description"],
					url=target["fieldUrls"][0].url,
					language=target["language"],
					startDate=target["crawlStartDateISO"],
					endDate=target["crawlEndDateISO"] 	
					)				

		else:
			print("Skipping...",c['title'])	


