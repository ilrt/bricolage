$(document).ready(function() {

	$('#btn-upload').bind('click', function(event) {
		event.preventDefault();
		BRIC.upload();
	});

	$('#btn-clear-ld-cache').bind('click', function(event) {
		BRIC.clearCache();
	});

	$('.btn-list-people').bind('click', function(event) {
		BRIC.listPeople();
	});

	$('#btn-list-archives').bind('click', function(event) {
		BRIC.list();
	});

	$('.close').bind('click', function() {
		$(event.target).parent().fadeOut();
	});

	$('#btn-link-save').bind('click', function(event) {
		BRIC.sameAs();
	});

	BRIC.list();
});

var BRIC = {};

BRIC.list = function() {

	$('#list-archives-working').show();

	var slowlyTimer = setTimeout(function() {
		$('#list-archives-working-slowly').show();
	}, 5000);

	var jqxhr = $
			.getJSON(
					'rest/archives',
					function(data) {

						$('#list-archives-working').hide();
						clearTimeout(slowlyTimer);
						$('#list-archives-working-slowly').hide();

						var items = [];

						if (data !== null) {
							if (Array.isArray(data)) {
								$.each(data, function(index, obj) {
									items.push(BRIC.getLi(obj));
								});
							}
						}

						$('div#list').html(items.join(''));

						$('.dropdown-toggle').dropdown();

						$('.stale-rdf')
								.popover(
										{
											title : 'Old RDF?',
											content : 'This RDF file is older than the corresponding EAD file. You may want to re-convert and re-publish.'
										});

						$('.named-graph').each(function() {
							$(this).popover({
								title : 'Named Graph',
								content : $(this).attr('ref')
							});
						});

						$('.actions').each(function() {
							BRIC.initActions($(this).attr('id'));
						});
					});
	jqxhr.error(function(jqXHR, status, error) {
		$('#list-archives-working').hide();
		clearTimeout(slowlyTimer);
		$('#list-archives-working-slowly').hide();
		BRIC.error("Error refreshing archives list. " + jqXHR.status + " - "
				+ error);
	});
};

BRIC.people = {};

BRIC.listPeople = function() {

	$('#list-people-working').show();

	var jqxhr = $.getJSON('rest/archives/people', function(data) {
		$('#list-people-working').hide();
		var items = [];

		if (data !== null) {
			if (Array.isArray(data)) {
				$.each(data, function(index, obj) {
					BRIC.people[obj['uri']] = obj;
					items.push(BRIC.getPersonLi(obj));
				});
			}
		}

		$('div#personList').html(items.join(''));

		BRIC.initPersonActions();

		// $('.dropdown-toggle').dropdown();
		//
		// $('.stale-rdf')
		// .popover(
		// {
		// title : 'Old RDF?',
		// content : 'This RDF file is older than the corresponding EAD file.
		// You may
		// want to re-convert and re-publish.'
		// });
		//
		// $('.named-graph').each(function() {
		// $(this).popover({
		// title : 'Named Graph',
		// content : $(this).attr('ref')
		// });
		// });
		//
		// $('.actions').each(function() {
		// BRIC.initActions($(this).attr('id'));
		// });
	});
	jqxhr.error(function(jqXHR, status, error) {
		$('#list-people-working').hide();
		BRIC.error("Error refreshing people list. " + jqXHR.status + " - "
				+ error);
	});
};

BRIC.getLi = function(archive) {
	var row = "<div class='row'>";
	row += "<div class='span3'>"
			+ "<span class='btn-group'>"
			+ "<button id='"
			+ archive['name']
			+ "'class='actions btn-mini dropdown-toggle' data-toggle='dropdown'>"
			+ "<i class='icon-cog'></i><span class='caret'></span>"
			+ "</button>" + "<ul class='dropdown-menu'>"
			+ BRIC.getActionsContent(archive['name']) + "</ul></span>  "
			+ archive['name'] + "</div>";
	row += "<div class='span1'>";
	if (archive['eadModified'] != '') {
		var d = new Date(0); // The 0 there is the key, which sets the date
		// to the epoch
		d.setUTCSeconds(archive['eadModified'] / 1000);
		var date = d.getDate();
		var month = d.getMonth() + 1; // Months are zero based
		var year = d.getFullYear();
		row += date + "/" + month + "/" + year;
	}
	row += "</div>";
	row += "<div class='span1'>";
	if (archive['eadFilename'] != '') {
		row += "<i class='icon-ok'></i><a href='rest/archives/ead/"
				+ archive['name']
				+ "' target='_blank'><i class='icon-download'></i></a>";
	} else {
		row += "<i class='icon-remove'></i>";
	}
	row += "</div>";
	row += "<div class='span1'>";
	if (archive['rdfFilename'] != '') {
		row += "<i class='icon-ok'></i><a href='rest/archives/rdf/"
				+ archive['name']
				+ "' target='_blank'><i class='icon-download'></i></a>";
		if (archive['eadModified'] > archive['rdfModified']) {
			row += " <i class='icon-warning-sign stale-rdf'></i>";
		}
	} else {
		row += "<i class='icon-remove'></i>";
	}
	row += "</div>";
	row += "<div class='span1'>";
	if (archive['published'] != '') {
		row += "<i class='icon-ok'></i><i ref='" + archive['published']
				+ "' class='icon-info-sign named-graph'></i>";
	} else if (archive['publishedMessage'] != '') {
		row += archive['publishedMessage'];
	} else {
		row += "<i class='icon-remove'></i>";
	}
	row += "</div>";
	row += "<div class='span4'>";
	if (archive['linkedDataUri'] != '') {
		row += "<a href='" + archive['linkedDataUri'] + "' target='_blank'>"
				+ archive['linkedDataUri'] + "</a>";
	}
	row += "</div>";
	row += "</div>";
	return row;
};

BRIC.getPersonLi = function(person) {

	var row = "<div class='row'>";
	row += "<div class='span2'>"
			+ "<span class='btn-group'>"
			+ "<button ref='"
			+ person['uri']
			+ "'class='actions btn-mini dropdown-toggle' data-toggle='dropdown'>"
			+ "<i class='icon-cog'></i><span class='caret'></span>"
			+ "</button>" + "<ul class='dropdown-menu'>"
			+ BRIC.getPersonActionsContent(person) + "</ul></span>  "
			+ person['name'] + "</div>";
	row += "<div class='span2'>";
	if (person['uri'] != '') {
		row += "<a target='_blank' href='" + person['uri']
				+ "'><i class='icon-ok'></i></a>";
	}
	row += "</div>";
	row += "<div class='span3'>";
	if (typeof person['sameas'] == 'object') {
		// array
		for ( var i = 0; i < person['sameas'].length; i++) {
			row += "<a target='_blank' href='" + person['sameas'][i] + "'>"
					+ person['sameas'][i] + "</a>";
			row += "<i uri='" + person['uri'] + "' ref='" + person['sameas'][i]
					+ "' class='remove-sameas icon-remove'/>";
			row += "<br/>";
		}
	}
	row += "</div>";
	row += "</div>";
	return row;
};

BRIC.upload = function() {
	var oData = new FormData(document.forms.namedItem("fileinfo"));

	BRIC.working("Uploading");

	var oReq = new XMLHttpRequest();
	oReq.open("POST", "rest/file/upload", true);
	oReq.onload = function(oEvent) {
		if (oReq.status == 200) {
			BRIC.success("Uploaded");
			BRIC.list();
		} else {
			BRIC.error("Error " + oReq.status
					+ " occurred uploading your file.");
		}
	};

	oReq.send(oData);
};

BRIC.sameAs = function() {
	BRIC.working("Asserting");

	var source = $('#viafModal > .modal-body > input[name="source"]').val();
	var target = "";
	$('#viafModal > .modal-body :checked').each(function() {
		target += $(this).val() + '\n';
	});
	console.log(target);
	$.ajax({
		type : 'POST',
		url : 'rest/link/sameas',
		data : {
			'source' : source,
			'target' : target
		},
		success : function() {
			$('#viafModal').modal('hide');
			BRIC.success("OK");
			BRIC.listPeople();
		}
	});

};

BRIC.removeSameAs = function(source, target) {
	BRIC.working("Removing");
	$.ajax({
		type : "DELETE",
		url : "rest/link/sameas/",
		data : {
			'source' : source,
			'target' : target
		},
		success : function() {
			BRIC.success("Deleted");
			BRIC.listPeople();
		},
		error : function(jqXHR, status, error) {
			BRIC.error('Delete failed. ' + jqXHR.responseText);
		}
	});
};

BRIC.initActions = function(id) {
};

BRIC.getActionsContent = function(id) {
	var ret = "<li><a href='#' onclick='BRIC.remove(" + id
			+ ");return false;'>Delete</a></li>";
	ret += "<li><a href='#' onclick='BRIC.toRDF(" + id
			+ ");return false;'>Convert to RDF</a></li>";
	ret += "<li><a href='#' onclick='BRIC.publish(" + id
			+ ");return false;'>Publish</a></li>";
	ret += "<li><a href='#' onclick='BRIC.unpublish(" + id
			+ ");return false;'>Unpublish</a></li>";
	return ret;
};

BRIC.initPersonActions = function() {
	$('.action-viaf').bind('click', function(event) {
		BRIC.viafSuggest(BRIC.people[$(this).attr('id')]);
	});
	$('.remove-sameas').bind('click', function(event) {
		BRIC.removeSameAs($(this).attr('uri'), $(this).attr('ref'));
	});
};

BRIC.getPersonActionsContent = function(person) {
	var ret = "<li><a href='#' class='action-viaf' id='" + person['uri']
			+ "'>VIAF lookup</a></li>";
	return ret;
};

BRIC.remove = function(id) {
	// id is HTML element
	id = id.getAttribute('id');

	BRIC.working("Deleting");

	$.ajax({
		type : "DELETE",
		url : "rest/archives/" + id,
		success : function() {
			BRIC.success("Deleted");
			BRIC.list();
		},
		error : function(jqXHR, status, error) {
			BRIC.error('Delete failed. ' + jqXHR.responseText);
		}
	});
};

BRIC.toRDF = function(id) {
	// id is HTML element
	id = id.getAttribute('id');

	BRIC.working("Transforming");

	$.ajax({
		type : "POST",
		url : "rest/archives/transform/" + id,
		success : function() {
			BRIC.success("Transformed");
			BRIC.list();
		},
		error : function(jqXHR, status, error) {
			BRIC.error('Transform failed. ' + jqXHR.responseText);
		}
	});
};

BRIC.publish = function(id) {
	// id is HTML element
	id = id.getAttribute('id');

	BRIC.working("Publishing");

	$.ajax({
		type : "POST",
		url : "rest/archives/publish/" + id,
		success : function() {
			BRIC.success("Published");
			BRIC.list();
		},
		error : function(jqXHR, status, error) {
			BRIC.error('Publish failed. ' + jqXHR.responseText);
		}
	});
};

BRIC.unpublish = function(id) {
	// id is HTML element
	id = id.getAttribute('id');

	BRIC.working("Unpublishing");

	$.ajax({
		type : "DELETE",
		url : "rest/archives/publish/" + id,
		success : function() {
			BRIC.success("Unpublished");
			BRIC.list();
		},
		error : function(jqXHR, status, error) {
			BRIC.error('Unpublish failed. ' + jqXHR.responseText);
		}
	});
};

BRIC.clearCache = function() {

	BRIC.working("Clearing cache");

	$.ajax({
		type : "DELETE",
		url : "rest/manager/clear-ld-cache",
		success : function() {
			BRIC.success("Cache cleared");
		},
		error : function(jqXHR, status, error) {
			BRIC.error('Cache clear failed. ' + jqXHR.responseText);
		}
	});
};

BRIC.viafSuggest = function(person) {

	BRIC.working("Contacting VIAF");

	$.ajax({
		dataType : "json",
		url : "rest/link/viaf-suggest/" + person['name'],
		success : function(json) {
			BRIC.success("OK");
			BRIC.showViafModal(person, json);
		},
		error : function(jqXHR, status, error) {
			BRIC.error('Error contacting VIAF. ' + jqXHR.responseText);
		}
	});
};

BRIC.showViafModal = function(person, json) {
	var html = "";
	$.each(json['result'], function(index, obj) {
		html += "<input type='checkbox' value='http://viaf.org/viaf/"
				+ obj['viafid'] + "'";
		if (BRIC.contains(person['sameas'], 'http://viaf.org/viaf/'
				+ obj['viafid'])) {
			html += " checked";
		}
		html += "/>";
		html += "<a href='http://viaf.org/viaf/" + obj['viafid']
				+ "' target='_blank'>" + obj['term'] + "</a><br/>";
	});
	html += "<input type='hidden' name='source' value='" + person['uri']
			+ "'/>";
	$('#viafModal > .modal-body').html(html);
	$('#viafModal').modal('show');
};

BRIC.info = function(msg) {
	BRIC.working();
	$('#alert-info-text').text(msg);
	$('.alert-info').show();
};

BRIC.success = function(msg) {
	BRIC.working();
	$('#alert-success-text').text(msg);
	$('.alert-success').show();
};

BRIC.error = function(msg) {
	BRIC.working();
	$('#alert-error-text').text(msg);
	$('.alert-error').show();
};

BRIC.working = function(msg) {
	$('.alert').hide();
	if (typeof msg === 'undefined') {
		$('.alert-working').hide();
	} else {
		$('#alert-working-text').text(msg);
		$('.alert-working').show();
	}
};

BRIC.contains = function(array, findValue) {
	if (!Array.isArray(array)) {
		return false;
	}
	var i = array.length;

	while (i--) {
		if (array[i] === findValue)
			return true;
	}
	return false;
};