<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fox="http://xmlgraphics.apache.org/fop/extensions">

	<!-- ======================================================= -->
	<!-- CONTROL VALUES -->
	<!-- ======================================================= -->

	<xsl:variable name="photocatcols">
		5
	</xsl:variable>

	<!-- ======================================================= -->
	<!-- TABULAR LAYOUT -->
	<!-- ======================================================= -->

	<xsl:template name="photocatrendercolumnheaders">
		<xsl:param name="remaining" />

		<xsl:if test="$remaining > 0">
			<fo:table-column column-width="proportional-column-width(1)" />
			<xsl:call-template name="photocatrendercolumnheaders">
				<xsl:with-param name="remaining" select="$remaining - 1" />
			</xsl:call-template>
		</xsl:if>

	</xsl:template>

	<!-- ======================================================= -->

	<xsl:template name="photocatrendercells">
		<xsl:param name="files" />
		<xsl:param name="remaining" />

		<xsl:if test="$remaining > 0">

			<fo:table-cell padding-bottom="0.1in">
				>

				<fo:block>
					<xsl:if test="count($files) > 0">

						<fo:block>
							<xsl:choose>
								<xsl:when test="$files[1]/thumbnailurl">
									<fo:external-graphic content-width="1.00in"
										border-width="1pt" border-style="solid">
										<xsl:attribute name="src">
						 	  	<xsl:value-of select="$files[1]/thumbnailurl" />
						 	  </xsl:attribute>
									</fo:external-graphic>
								</xsl:when>
								<xsl:otherwise>
									<fo:block background-color="red" color="white"
										font-weight="bold" margin="10pt" padding="10pt">
										The media file was damaged and/or a
										thumbnail was unable to be produced
										for the data in this file.
									</fo:block>
								</xsl:otherwise>
							</xsl:choose>

							<fo:block>
								
								<fo:inline font-family="monospace">
									<xsl:choose>
										<xsl:when test="string-length($files[1]/name) > 16">
											<xsl:attribute name="font-size">4pt</xsl:attribute>
										</xsl:when>
										<xsl:otherwise>
											<xsl:attribute name="font-size">7pt</xsl:attribute>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:value-of select="$files[1]/name" />
								</fo:inline>
								<xsl:if test="$files[1]/datatype = 'MOVIE'">
									<fo:inline font-size="7pt" color="orange">
										(Movie)
									</fo:inline>
								</xsl:if>
							</fo:block>

							<xsl:if test="$files[1]/description">
								<fo:block font-size="4pt">
									<xsl:value-of select="$files[1]/description" />
								</fo:block>
							</xsl:if>

							<fo:block font-size="7pt">
								<xsl:value-of select="$files[1]/timestamp" />
							</fo:block>

						</fo:block>

					</xsl:if>
				</fo:block>
			</fo:table-cell>

			<xsl:call-template name="photocatrendercells">
				<xsl:with-param name="remaining" select="$remaining - 1" />
				<xsl:with-param name="files" select="$files[position() > 1]" />
			</xsl:call-template>

		</xsl:if>

	</xsl:template>

	<!-- ======================================================= -->

	<xsl:template name="photocatrenderrows">
		<xsl:param name="files" />

		<xsl:if test="count($files) > 0">

			<fo:table-row>

				<xsl:call-template name="photocatrendercells">
					<xsl:with-param name="remaining" select="$photocatcols" />
					<xsl:with-param name="files" select="$files" />
				</xsl:call-template>

			</fo:table-row>

			<xsl:call-template name="photocatrenderrows">
				<xsl:with-param name="files"
					select="$files[position() > $photocatcols]" />
			</xsl:call-template>

		</xsl:if>

	</xsl:template>

	<!-- ======================================================= -->
	<!-- MAIN PAGE -->
	<!-- ======================================================= -->

	<xsl:template match="/photocat">

		<fo:root>

			<fo:layout-master-set>

				<!-- layout information -->
				<fo:simple-page-master master-name="simple"
					page-height="29.7cm" page-width="21cm" margin-top="0.5cm"
					margin-bottom="0.5cm" margin-left="0.5cm" margin-right="0.5cm">
					<fo:region-body />
					<fo:region-before extent="0.5cm" />
					<fo:region-after extent="0.5cm" />
				</fo:simple-page-master>
			</fo:layout-master-set>
			<!-- end: defines page layout -->

			<!-- This is a tree structure which defines the bookmarks. The bookmarks 
				are just the sections which are already defined in the XML. -->

			<fo:bookmark-tree>

				<xsl:for-each select="section">

					<fo:bookmark>
						<xsl:attribute name="internal-destination">
  						<xsl:value-of select="@code" />
  					</xsl:attribute>

						<fo:bookmark-title>
							<xsl:value-of select="@label" />
						</fo:bookmark-title>

					</fo:bookmark>

				</xsl:for-each>

			</fo:bookmark-tree>

			<fo:page-sequence master-reference="simple">
				<fo:flow flow-name="xsl-region-body" font-size="10pt">

					<xsl:for-each select="section">

						<fo:block space-before="10mm" space-after="10mm" padding="5mm"
							font-size="18pt" background-color="gray" color="white">
							<xsl:attribute name="id">
       			<xsl:value-of select="@code" />
       		</xsl:attribute>
							<xsl:value-of select="@label" />
						</fo:block>

						<fo:table width="100%" table-layout="fixed">

							<xsl:call-template name="photocatrendercolumnheaders">
								<xsl:with-param name="remaining" select="$photocatcols" />
							</xsl:call-template>

							<fo:table-body>
								<xsl:call-template name="photocatrenderrows">
									<xsl:with-param name="files" select="file" />
								</xsl:call-template>
							</fo:table-body>

						</fo:table>

					</xsl:for-each>

				</fo:flow>
			</fo:page-sequence>

		</fo:root>

	</xsl:template>

</xsl:stylesheet>