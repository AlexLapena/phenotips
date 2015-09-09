package org.phenotips.vocabularies.rest;

import org.phenotips.vocabularies.rest.model.VocabulariesRep;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
/**
 * Root resource for working with vocabularies.
 *
 * @version
 * @since
 */
@Path("/vocabularies")
public interface VocabulariesResource
{
    @GET VocabulariesRep getAllVocabularies();
}
