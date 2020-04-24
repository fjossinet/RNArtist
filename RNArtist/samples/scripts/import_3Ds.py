#!/usr/bin/env python

"""
Annotate and store solved 3D structures into MongoDB

To use this script, you will need the following python3 modules:
- pandas (conda install pandas)
- pymongo (conda install pymongo)
- docker (conda install docker)
- mongodb (conda install -c anaconda mongodb)

Start your MongoDB first: mongod --dbpath ~/tmp/mongodb

Then run your script to import and annotate 3Ds: ./import_3Ds.py -annotate    
"""

#built-in packages
import sys, os, math, datetime, re, urllib, subprocess, random, string
from itertools import groupby
from operator import itemgetter,attrgetter

#additional packages
from bson.objectid import ObjectId
from pymongo import MongoClient
from pandas import DataFrame
import docker

def generate_random_name(n):
    """
    Generate a random name of n letters
    """
    return ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(n))

def is_canonical(residue_1, residue_2, orientation, edge_1, edge_2):
    return (residue_1.upper() == 'A' and residue_2.upper() == 'U' or residue_1.upper() == 'U' and residue_2.upper() == 'A' or\
            residue_1.upper() == 'G' and residue_2.upper() == 'C' or residue_1.upper() == 'C' and residue_2.upper() == 'G' or\
            residue_1.upper() == 'G' and residue_2.upper() == 'U' or residue_1.upper() == 'U' and residue_2.upper() == 'G') and\
            orientation.lower() == 'c' and edge_1.upper() == '(' and edge_2.upper() == ')'

class RNA3DHub:

    """
    Wrapper for the RNA3DHub database (http://rna.bgsu.edu/rna3dhub/)
    """

    def __init__(self, release="current"):
        self.release = release

    def get_clusters(self, resolution=2.5):
        """
        Returns the clusters of solved 3Ds from the non-redundant list provided by http://rna.bgsu.edu/rna3dhub/nrlist

        Parameters:
        -resolution: Default value is 2.5

        A pandas DataFrame whose columns are:
        - the cluster id
        - the list of pdb ids
        """
        rows = []
        response = urllib.request.urlopen("http://rna.bgsu.edu/rna3dhub/nrlist/download/"+str(self.release)+"/"+str(resolution)+"A/csv")
        content = response.read().decode('utf-8')
        for line in content.split('\n'):
            if len(line.strip()) > 0:
                tokens = line.split(',')
                pdb_ids = ' '.join(tokens[1:])
                rows.append([re.sub('"', '', tokens[0]), re.sub('"', '', pdb_ids).split(' ')]);
        return DataFrame(rows, columns=['cluster-id', 'pdb-ids'])

class PDBQuery:
    """
    A utility class to use the method query from the PDB class (see PDB class in module pyrna.db).
    """
    def __init__(self, min_res = '0.1', max_res = '3.0', min_date = None, max_date = None, keywords = [], authors = [], pdb_ids = [], title_contains = [], contains_rna = 'Y', contains_protein = 'Y', contains_dna = 'N', contains_hybrid = 'N', experimental_method = 'X-RAY'):
        self.min_res = min_res
        self.max_res = max_res
        self.min_date = min_date
        self.max_date = max_date
        self.keywords = keywords
        self.authors = authors
        self.pdb_ids = pdb_ids
        self.title_contains = title_contains
        self.contains_rna = contains_rna
        self.contains_protein = contains_protein
        self.contains_dna = contains_dna
        self.contains_hybrid = contains_hybrid
        self.experimental_method = experimental_method

class PDB:
    """
    Wrapper for the PDB database http://www.rcsb.org/
    """

    def __init__(self):
        pass

    def get_entry(self, pdb_id):
        """
        Return the content of a PDB entry as a string
        """
        response = urllib.request.urlopen("http://www.rcsb.org/pdb/download/downloadFile.do?fileFormat=pdb&compression=NO&structureId=%s"%pdb_id)
        content = response.read().decode('utf-8')
        return content

    def query(self, query):
        """
        Returns a list of PDB ids in answer to the query

        Parameters:
        - query: the query as a PDBQuery object or as a String (copy/pasted from the PDB website http://www.rcsb.org/pdb/software/rest.do#search)

        Returns:
        - a list of pdb ids
        """
        post_data = None
        if type(query) == str:
            post_data = query
        else:
            min_res = query.min_res or '0.1'
            max_res = query.max_res or '3.0'
            min_date = query.min_date or None
            max_date = query.max_date or None
            keywords = query.keywords or []
            authors = query.authors or []
            pdb_ids = query.pdb_ids or []
            title_contains = query.title_contains or []
            contains_rna = query.contains_rna or 'Y'
            contains_protein = query.contains_protein or 'Y'
            contains_dna = query.contains_dna or 'N'
            contains_hybrid = query.contains_hybrid or 'N'
            experimental_method = query.experimental_method or 'X-RAY'
            post_data = '<?xml version="1.0" encoding="UTF-8"?><orgPdbCompositeQuery version="1.0">'
            ids = []
            refinementLevel = 0

            if experimental_method == 'X-RAY' and (max_res or min_res):
                if refinementLevel:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel><conjunctionType>and</conjunctionType>'
                else:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel>'
                post_data += '\
    <orgPdbQuery>\
    <version>head</version>\
    <queryType>org.pdb.query.simple.ResolutionQuery</queryType>\
    <description>Resolution query</description>\
    <refine.ls_d_res_high.comparator>between</refine.ls_d_res_high.comparator>'

                if min_res:
                    post_data += '\
    <refine.ls_d_res_high.min>'+min_res+'</refine.ls_d_res_high.min>'

                if max_res:
                    post_data += '\
    <refine.ls_d_res_high.max>'+max_res+'</refine.ls_d_res_high.max>'

                post_data += '</orgPdbQuery></queryRefinement>'
                refinementLevel += 1

            if max_date or min_date:
                if refinementLevel:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel><conjunctionType>and</conjunctionType>'
                else:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel>'

                post_data +='\
    <orgPdbQuery>\
    <version>head</version>\
    <queryType>org.pdb.query.simple.ReleaseDateQuery</queryType>\
    <description>Release Date query</description>\
    <refine.ls_d_res_high.comparator>between</refine.ls_d_res_high.comparator>'

                if min_date:
                    post_data += '\
    <database_PDB_rev.date.min>'+min_date+'</database_PDB_rev.date.min>'

                if max_date:
                    post_data += '\
    <database_PDB_rev.date.max>'+max_date+'</database_PDB_rev.date.max>'

                post_data += '</orgPdbQuery></queryRefinement>'
                refinementLevel += 1

            for i in range(0, len(title_contains)):
                titleContain = title_contains[i]
                if refinementLevel:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel><conjunctionType>and</conjunctionType>'
                else:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel>'
                post_data += '\
    <orgPdbQuery>\
    <version>head</version>\
    <queryType>org.pdb.query.simple.StructTitleQuery</queryType>\
    <description>StructTitleQuery: struct.title.comparator=contains struct.title.value='+titleContain+'</description>\
    <struct.title.comparator>contains</struct.title.comparator>\
    <struct.title.value>'+titleContain+'</struct.title.value>\
    </orgPdbQuery></queryRefinement>'
                refinementLevel += 1

            if keywords:
                if refinementLevel:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel><conjunctionType>and</conjunctionType>'
                else:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel>'
                post_data += '\
    <orgPdbQuery>\
    <version>head</version>\
    <queryType>org.pdb.query.simple.AdvancedKeywordQuery</queryType>\
    <description>Text Search for: '+" ".join(keywords)+'</description>\
    <keywords>'+" ".join(keywords)+'</keywords>\
    </orgPdbQuery></queryRefinement>'
                refinementLevel += 1

            if pdb_ids:
                if refinementLevel:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel><conjunctionType>and</conjunctionType>'
                else:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel>'

                post_data += '\
    <orgPdbQuery>\
    <version>head</version>\
    <queryType>org.pdb.query.simple.StructureIdQuery</queryType>\
    <description>Simple query for a list of PDB IDs ('+str(len(pdb_ids))+' IDs) :'+", ".join(pdb_ids)+'</description>\
    <structureIdList>'+", ".join(pdb_ids)+'</structureIdList>\
    </orgPdbQuery></queryRefinement>'
                refinementLevel += 1

            if experimental_method:
                if refinementLevel:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel><conjunctionType>and</conjunctionType>'
                else:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel>'

                post_data += '\
    <orgPdbQuery>\
    <version>head</version>\
    <queryType>org.pdb.query.simple.ExpTypeQuery</queryType>\
    <description>Experimental Method is '+experimental_method+'</description>\
    <mvStructure.expMethod.value>'+experimental_method+'</mvStructure.expMethod.value>\
    </orgPdbQuery></queryRefinement>'
                refinementLevel += 1

            for i in range(0, len(authors)):
                author = authors[i]
                if refinementLevel:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel><conjunctionType>and</conjunctionType>'
                else:
                    post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel>'

                post_data += '\
    <orgPdbQuery>\
    <version>head</version>\
    <queryType>org.pdb.query.simple.AdvancedAuthorQuery</queryType>\
    <description>Author Search: Author Search: audit_author.name='+author+' OR (citation_author.name='+author+' AND citation_author.citation_id=primary)</description>\
    <exactMatch>false</exactMatch>\
    <audit_author.name>'+author+'</audit_author.name>\
    </orgPdbQuery></queryRefinement>'
                refinementLevel += 1

            #chain type
            if refinementLevel:
                post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel><conjunctionType>and</conjunctionType>'
            else:
                post_data += '<queryRefinement><queryRefinementLevel>'+str(refinementLevel)+'</queryRefinementLevel>'

            post_data += '\
    <orgPdbQuery>\
    <version>head</version>\
    <queryType>org.pdb.query.simple.ChainTypeQuery</queryType>\
    <description>Chain Type</description>\
    <contains_protein>'+contains_protein+'</contains_protein>\
    <contains_dna>'+contains_dna+'</contains_dna>\
    <contains_rna>'+contains_rna+'</contains_rna>\
    <contains_hybrid>'+contains_hybrid+'</contains_hybrid>\
    </orgPdbQuery></queryRefinement>'
            refinementLevel += 1

            post_data += '</orgPdbCompositeQuery>'

        f = urllib.request.urlopen("http://www.rcsb.org/pdb/rest/search", post_data.encode("utf-8"))

        result = f.read().decode('utf-8')

        if result:
           ids = result.split('\n')[:-1]

        return ids

class Rnaview:
    """
    Application Controller for RNAVIEW.
    """
    def __init__(self, cache_dir="/tmp"):
        self.cache_dir = cache_dir
        if not os.path.exists(self.cache_dir):
            os.mkdir(self.cache_dir)
        self.docker_client = docker.from_env()

    def annotate(self, tertiary_structure = None, pdb_content = None, canonical_only = False, raw_output = False):
        """
        Parameters:
        -----------
        - tertiary_structure (default: None): a TertiaryStructure object (see pyrna.features)
        - pdb_content (default: None): the content of a PDB file
        - canonical_only (default: False): if set to True, the helices will be made exclusively with canonical base-pairs: AU c(), GC c() or GU c().
        - raw_output (default: False): if set to True, the method returns the raw RNAML result produced with RNAVIEW .
        """
        if not tertiary_structure and not pdb_content:
            raise Exception("No data provided")
        pdb_file_name = generate_random_name(7)+'.pdb'
        with open(self.cache_dir+'/'+pdb_file_name, 'w') as pdb_file:
            if pdb_content:
                pdb_file.write(pdb_content)
            else:
                pdb_file.write(to_pdb(tertiary_structure, export_numbering_system = True))
        subprocess.getoutput("docker run -v %s:/data fjossinet/assemble2 rnaview -p /data/%s"%(self.cache_dir,pdb_file_name))

        xml_file_name = self.cache_dir+'/'+pdb_file_name+".xml"
        xml_content = ""
        if os.path.exists(xml_file_name):
            with open(xml_file_name) as xml_file:
                xml_content = xml_file.read()
        else:
            raise Exception("No file %s"%xml_file_name)
        if raw_output:
            return xml_content
        else:
            import xml.etree.ElementTree as ET
            rnaml_tree = ET.fromstring(xml_content)

            molecule = rnaml_tree.find('molecule')

            rna = RNA(name = tertiary_structure.rna.name, sequence = re.sub('\s+','',molecule.find('sequence').find('seq-data').text))

            secondary_structure = SecondaryStructure(rna)
            secondary_structure.source='tool:rnaview:N.A.'
            new_3D = None

            if len(rna) != len(tertiary_structure.rna): #RNAVIEW can have problems with some residues. Consequently, RNAVIEW produces an RNA molecule with a different sequence. We need to fit the 3D to this molecule.
                new_3D = TertiaryStructure(rna)
                new_3D.source = "tool:rnaview:N.A."
                numbering_system = re.sub('\s{2,}', ' ', molecule.find('sequence').find('numbering-table').text).strip().split(' ')
                #the strategy is the following:
                #- the numbering-table in the XML output stores the labels of the 3D residues used by RNAVIEW
                #- for each residue label, we recover its absolute position in the numbering system of the initial 3D
                residue_absPos = 1
                for residue_label in numbering_system:
                    for absPos, label in list(tertiary_structure.numbering_system.items()):
                        if label == residue_label:
                            new_3D.residues[residue_absPos] = tertiary_structure.residues[int(absPos)]
                            break
                    residue_absPos += 1
            else: #no problem, then we can substitute the RNA of the 2D for the RNA of the 3D
                secondary_structure.rna = tertiary_structure.rna

            if not canonical_only:

                for helix in molecule.find('structure').find('model').find('str-annotation').findall('helix'):
                    secondary_structure.add_helix(helix.get('id'), int(helix.find('base-id-5p').find('base-id').find('position').text), int(helix.find('base-id-3p').find('base-id').find('position').text), int(helix.find('length').text));

                #for single_strand in molecule.find('structure').find('model').find('str-annotation').findall('single-strand'):
                #    end5 = int(single_strand.find('segment').find('base-id-5p').find('base-id').find('position').text)
                #    end3 = int(single_strand.find('segment').find('base-id-3p').find('base-id').find('position').text)
                #    secondary_structure.add_single_strand(single_strand.find('segment').find('seg-name').text, end5, end3-end5+1);

                for base_pair in molecule.find('structure').find('model').find('str-annotation').findall('base-pair'):
                    edge1 = '('
                    edge2 = ')'
                    if base_pair.find('edge-5p').text == 'H':
                        edge1 = '['
                    elif base_pair.find('edge-5p').text == 'S':
                        edge1 = '{'
                    elif base_pair.find('edge-5p').text == 's':
                        edge1 = '{'
                    elif base_pair.find('edge-5p').text == '!':
                        edge1 = '!'

                    if base_pair.find('edge-3p').text == 'H':
                        edge2 = ']'
                    elif base_pair.find('edge-3p').text == 'S':
                        edge2 = '}'
                    elif base_pair.find('edge-3p').text == 's':
                        edge2 = '}'
                    elif base_pair.find('edge-3p').text == '!':
                        edge2 = '!'

                    secondary_structure.add_base_pair(base_pair.find('bond-orientation').text.lower(), edge1, edge2, int(base_pair.find('base-id-5p').find('base-id').find('position').text), int(base_pair.find('base-id-3p').find('base-id').find('position').text));

            else: #EXPERIMENTAL, STILL WITH BUGS!!! 
                #we need to produce a new secondary structure made with canonical bps only
                canonical_bps = []
                non_canonical_bps = []
                for base_pair in molecule.find('structure').find('model').find('str-annotation').findall('base-pair'):
                    edge1 = '('
                    edge2 = ')'
                    if base_pair.find('edge-5p').text == 'H':
                        edge1 = '['
                    elif base_pair.find('edge-5p').text == 'S':
                        edge1 = '{'
                    elif base_pair.find('edge-5p').text == 's':
                        edge1 = '{'
                    elif base_pair.find('edge-5p').text == '!':
                        edge1 = '!'

                    if base_pair.find('edge-3p').text == 'H':
                        edge2 = ']'
                    elif base_pair.find('edge-3p').text == 'S':
                        edge2 = '}'
                    elif base_pair.find('edge-3p').text == 's':
                        edge2 = '}'
                    elif base_pair.find('edge-3p').text == '!':
                        edge2 = '!'

                    orientation = base_pair.find('bond-orientation').text.lower()

                    pos1 = int(base_pair.find('base-id-5p').find('base-id').find('position').text)
                    residue1 = secondary_structure.rna.sequence[pos1-1]
                    pos2 = int(base_pair.find('base-id-3p').find('base-id').find('position').text)
                    residue2 = secondary_structure.rna.sequence[pos2-1]

                    canonical_bps.append([orientation, edge1, edge2, pos1, pos2]) if is_canonical(residue1, residue2, orientation, edge1, edge2) else non_canonical_bps.append([orientation, edge1, edge2, pos1, pos2])

                secondary_structure = base_pairs_to_secondary_structure(secondary_structure.rna, DataFrame(canonical_bps, columns=['orientation', 'edge1', 'edge2', 'pos1', 'pos2']))

                for bp in non_canonical_bps: #the non-canonical interactions are tertiary ones
                    secondary_structure.add_tertiary_interaction(bp[0], bp[1], bp[2], bp[3], bp[4])

            secondary_structure.find_single_strands()

            if new_3D:
                return (secondary_structure, new_3D)
            else:
                return (secondary_structure, tertiary_structure)

class Block:
    """
    A continuous range of molecular positions, with a single start and end point.
    """
    def __init__(self, start, end):
        if start < end:
            self.start = start
            self.end = end
        else:
            self.start = end
            self.end = start

    def is_before(self, block):
        pass

    def is_beside(self, block):
        pass

    def intersects(self, block):
        pass

    def merge(self, block):
        pass

class Location:
    """
    A Location defines a range of molecular positions, continuous or not. A location is made with Block objects.
    """
    def __init__(self, start = None, end = None, single_positions = None, nested_lists = None):
        """
        To instantiate a Location, you can:
        - set a start and end position: Location(start=34, end=69). The location will contain all the positions between the start end end.
        - list all the single positions (sorted or not) to be contained in the location: Location(single_positions=[34, 56, 57, 58, 67, 68, 69])
        - list the ranges of continuous positions as nested lists: Location(nested_lists=[[34,34], [56,58], [67,69]])
        """
        self.blocks = []
        if start and end:
            self.add_block(Block(start, end))
        elif single_positions:
            single_positions.sort()
            for k, g in groupby(enumerate(single_positions), lambda ix : ix[0] - ix[1]):
                _range = list(map(itemgetter(1), g))
                self.blocks.append(Block(min(_range), max(_range)))
        elif nested_lists:
            for nested_list in nested_lists:
                self.blocks.append(Block(min(nested_list), max(nested_list)))

    def add_block(self, block):
        blocks_to_remove = []

        for _block in self.blocks:
            if block.is_before(_block) and not block.is_beside(_block):
                break
            elif block.intersects(_block) or block.is_beside(_block):
                block.merge(_block)
                blocks_to_remove.append(_block)
                #its necessary to continue to see if the new Block can merge with other blocks
                continue
            elif len(blocks_to_remove):
                break

        for block_to_remove in blocks_to_remove:
            self.blocks.remove(block_to_remove)

        self.blocks.append(block)
        self.blocks = sorted(self.blocks, key=lambda block: block.start)

    def remove_location(self, location):
        """
        Return a new Location object from the difference between the current Location and the Location given as argument.
        Difference means all the positions not found in the Location given as argument
        """
        single_positions_1 = self.get_single_positions()
        single_positions_2 = location.get_single_positions()

        diff = list(set(single_positions_1) - set(single_positions_2))

        return Location(single_positions = diff)

    def remove_locations(self, locations):
        """
        Return a new Location object from the difference between the current Location with all the Locations given in a list as argument.
        Difference means all the positions not found in the Locations given as argument
        """
        single_positions_1 = self.get_single_positions()
        single_positions_2 = []

        for location in locations:
            single_positions_2 += location.get_single_positions()

        diff = list(set(single_positions_1) - set(single_positions_2))

        return Location(single_positions = diff)


    def get_single_positions(self):
        """
        Returns:
        ------
        all the single positions making this Location as a list.
        """
        single_positions = []
        for block in self.blocks:
            single_positions += range(block.start, block.end+1)
        return single_positions

    def has_position(self, position):
        """
        Test if the location encloses a single position.
        Parameters:
        ---------
        position: an integer
        """
        return position in self.get_single_positions()

    def start(self):
        return self.blocks[0].start

    def end(self):
        return self.blocks[-1].end


class Molecule:
    def __init__(self, name):
        self._id = str(ObjectId())
        self.modified_residues = []
        self.name = name
        self.family = None
        self.organism = None
        self.lineage = None
        self.source = 'N.A.:N.A.:N.A.'
        self.sequence = ""
        self.dbxref = [] #to store the references, as strings, to external databases for this molecule ("RFAM:RF00001", "GO:0006355", "GeneID:13886572")

    def get_gaps_positions(self):
        positions = []
        i = 0
        for c in list(self.sequence):
            if c == '-':
                positions.append(i)
            i += 1
        return positions

    def to_fasta(self, single_line=False):
        lines = []
        lines.append(">" + self.name)
        if single_line:
            lines.append(self.sequence)
        else:
            c = 0
            while c < len(self.sequence):
                d = min(len(self.sequence), c + 79)
                lines.append(self.sequence[c:d])
                c += 79
        return '\n'.join(lines)

    def _repr_html_(self):
        subsequences = [''.join(subsequence) for subsequence in chunks(list(self.sequence),60)]
        html = "<pre>"
        i = 0
        for subsequence in subsequences:
            html += str(i*60+1)+"\t"+re.sub('U', '<font color="green">U</font>', re.sub('T', '<font color="green">T</font>', re.sub('C', '<font color="orange">C</font>', re.sub('G', '<font color="red">G</font>', re.sub('A', '<font color="blue">A</font>',subsequence)))))+"\n"
            i += 1
        html += "</pre>"
        return html

    def __add__(self, seq):
        if seq.__class__ == str:
            self.sequence = ''.join([self.sequence, seq])

    def __sub__(self, length):
        if length.__class__ == int and length <= len(self.sequence):
            self.sequence = self.sequence[0: len(self.sequence)-length]

    def __len__(self):
        return len(self.sequence)

    def __iter__(self):
        return iter(self.sequence)

    def __getslice__(self, i, j):
        return self.sequence.__getslice__(i, j)

    def __getitem__(self, i):
        return self.sequence.__getitem__(i)

class DNA(Molecule):
    def __init__(self, sequence, name = 'dna'):
        Molecule.__init__(self, name)
        self.sequence = sequence

    def get_complement(self):
        """
        Returns:
        ------
        the complement sequence as a string.
        """
        basecomplement = {'A': 'T', 'C': 'G', 'G': 'C', 'T': 'A'}
        letters = list(self.sequence)
        letters = [basecomplement[base] if base in basecomplement else base for base in letters]
        return ''.join(letters)

class RNA(Molecule):
    def __init__(self, sequence, name = 'rna'):
        Molecule.__init__(self, name)

        for residue in list(sequence):
            self.add_residue(residue)

    def add_residue(self, residue):
        if residue in modified_ribonucleotides:
            self.modified_residues.append((residue, len(self.sequence)+1))
            residue = modified_ribonucleotides[residue]
        if residue in ['A', 'U', 'G', 'C']:
            self.sequence = ''.join([self.sequence, residue])
        elif residue in ['.', '_', '-']:
            self.sequence = ''.join([self.sequence, '-'])
        else:
            #print "Unknown residue "+residue
            self.sequence = ''.join([self.sequence, residue])

    def get_complement(self):
        """
        Returns:
        ------
        the complement sequence as a string.
        """
        basecomplement = {'A': 'U', 'C': 'G', 'G': 'C', 'U': 'A'}
        letters = list(self.sequence)
        letters = [basecomplement[base] if base in basecomplement else base for base in letters]
        return ''.join(letters)

class Protein(Molecule):
    def __init__(self, sequence, name = 'protein'):
        Molecule.__init__(self, name)

        for residue in list(sequence):
            self.add_residue(residue)

    def add_residue(self, residue):
        if residue in modified_aminoacids:
            self.modified_residues.append((residue, len(self.sequence)+1))
            residue = modified_aminoacids[residue]
        self.sequence = ''.join([self.sequence, residue])

class SecondaryStructure:

    def __init__(self, rna):
        self.name = "2D"
        self.rna = rna
        self.helices = []
        self.single_strands = []
        self.tertiary_interactions = []
        self.junctions = []
        self.stem_loops = []
        self.source = "N.A:N.A:N.A"
        self._id = str(ObjectId())
        self.__step = None

    def get_junctions(self):
        return DataFrame(self.junctions)

    def get_paired_residue(self, pos):
        for helix in self.helices:
            if pos >= helix['location'][0][0] and pos <= helix['location'][0][0] + helix['length']-1:
                return helix['location'][-1][-1] - (pos-helix['location'][0][0])
            elif pos <= helix['location'][-1][-1] and pos >= helix['location'][-1][-1] - helix['length']+1:
                return helix['location'][0][0]+ helix['location'][-1][-1] - pos
        return -1

    def find_single_strands(self):
        full_location = Location(start = 1, end = len(self.rna))
        for helix in self.helices:
            full_location =  full_location.remove_location(Location(start = helix['location'][0][0], end = helix['location'][0][-1]))
            full_location =  full_location.remove_location(Location(start = helix['location'][-1][0], end = helix['location'][-1][-1]))
        single_positions = full_location.get_single_positions()
        single_positions.sort()

        start = None
        length = 0
        single_strand_count = 1
        for index, current_pos in enumerate(single_positions):
            if index == 0 or current_pos == single_positions[index-1]+1:
                length += 1
                if index == 0:
                    start = current_pos
            else:
                self.add_single_strand("SS_%i"%single_strand_count, start, length)
                single_strand_count +=1
                length = 1
                start = current_pos
        #the last
        self.add_single_strand("SS_%i"%single_strand_count, start, length)

    def find_junctions(self):
        self.junctions = []
        for single_strand in self.single_strands:
            if single_strand['location'][0] == 1 or single_strand['location'][-1] == len(self.rna) or len([junction for junction in self.junctions if single_strand in junction['single_strands']]):
                continue
            strands = [single_strand]
            descr = self.rna[single_strand['location'][0]-1:single_strand['location'][-1]]+" "
            current_pos =  self.get_paired_residue(single_strand['location'][-1]+1)+1
            location = [[single_strand['location'][0]-1, single_strand['location'][-1]+1]]
            next_single_strand = None

            while current_pos >= 1 and current_pos <= len(self.rna):
                next_single_strand = [single_strand for single_strand in self.single_strands if single_strand['location'][0] == current_pos]
                if next_single_strand and next_single_strand[0] == single_strand:
                    break
                elif next_single_strand:
                    strands.append(next_single_strand[0])
                    location.append([next_single_strand[0]['location'][0]-1, next_single_strand[0]['location'][-1]+1])
                    descr += self.rna[next_single_strand[0]['location'][0]-1:next_single_strand[0]['location'][-1]]+" "
                    current_pos = self.get_paired_residue(next_single_strand[0]['location'][-1]+1)+1
                    continue
                next_helix = [helix for helix in self.helices if current_pos == helix['location'][0][0] or current_pos == helix['location'][-1][-1]-helix['length']+1]
                if next_helix:
                    descr += '- '
                    location.append([current_pos-1, current_pos])
                    current_pos = self.get_paired_residue(current_pos)+1

            if next_single_strand and next_single_strand[0] == single_strand:
                self.junctions.append({
                    'single_strands': strands,
                    'description': descr.strip(),
                    'location': location
                })

        #now we search for junctions with only directly linked helices
        for helix in self.helices:
            if helix['location'][0][0] == 1 or helix['location'][-1][-1] == len(self.rna) or len([junction for junction in self.junctions if helix['location'][0][0] in sum(junction['location'],[]) or helix['location'][-1][-1] in sum(junction['location'],[])]):
                continue
            descr = ""
            location = []
            next_helix = None
            current_pos = helix['location'][-1][-1]+1

            while current_pos >= 1 and current_pos <= len(self.rna):
                next_helix = [helix for helix in self.helices if current_pos == helix['location'][0][0] or current_pos == helix['location'][1][0]]
                if next_helix and next_helix[0] == helix:
                    descr += '- '
                    location.append([current_pos-1, current_pos])
                    break
                elif next_helix:
                    descr += '- '
                    location.append([current_pos-1, current_pos])
                    current_pos = self.get_paired_residue(current_pos)+1
                else:
                    break

            if next_helix and next_helix[0] == helix:
                self.junctions.append({
                    'single_strands': [],
                    'description': descr.strip(),
                    'location': location
                })

            #the other side
            descr = ""
            location = []
            next_helix = None
            current_pos = helix['location'][0][1]+1

            while current_pos >= 1 and current_pos <= len(self.rna):
                next_helix = [helix for helix in self.helices if current_pos == helix['location'][0][0] or current_pos == helix['location'][1][0]]
                if next_helix and next_helix[0] == helix:
                    descr += '- '
                    location.append([current_pos-1, current_pos])
                    break
                elif next_helix:
                    descr += '- '
                    location.append([current_pos-1, current_pos])
                    current_pos = self.get_paired_residue(current_pos)+1
                else:
                    break

            if next_helix and next_helix[0] == helix:
                self.junctions.append({
                    'single_strands': [],
                    'description': descr.strip(),
                    'location': location
                })

        self.junctions = sorted(self.junctions, key=lambda x: x['location'][0][0])

    def find_stem_loops(self):
        if not self.junctions:
            self.find_junctions()
        #we search for all the stem-loops. A stem loop is a set of contigous helices linked with inner loops and with an apical loop at one end.
        self.stem_loops = []
        ranges = []
        for helix in self.helices:
            #print "helix",helix['location']
            start = helix['location'][0][0]
            end = helix['location'][-1][-1]
            #if the helix ends are linked to a junction of degree >= 3 or not linked to any junction, this is a range to keep.
            linked_to_a_junction = False
            for junction in self.junctions:
                for i in range(0, len(junction['location'])-1):
                    if start == junction['location'][i][-1] and end == junction['location'][i+1][0]:
                        if len(junction['location']) >= 3:
                            ranges.append([start, end])
                        linked_to_a_junction = True
                if start == junction['location'][-1][-1] and end == junction['location'][0][0]: #we test the last two ends of the location (first and last values of the matrix)
                    if len(junction['location']) >= 3:
                        ranges.append([start, end])
                    linked_to_a_junction = True
            if not linked_to_a_junction:
                ranges.append([start, end])
        #print ranges

        for _range in ranges:
            start = _range[0]
            end = _range[1]
            #print '\n\n', "search between: ", start,"-", end, '\n\n'
            enclosed_apical_loops = []
            enclosed_junctions = []
            enclosed_inner_loops = []
            enclosed_helices = []
            for _junction in self.junctions:
                _start = min(_junction['location'])[0] #the lowest end
                _end = max(_junction['location'])[-1] #the highest end
                if _start > start and _end < end:
                    if len(_junction['location']) == 1:
                        enclosed_apical_loops.append(_junction)
                        #print "found apical loop at ", _start, _end
                    elif len(_junction['location']) == 2:
                        enclosed_inner_loops.append(_junction)
                        #print "found inner loop at ", _start, _end
                        #print _junction['location']
                    elif len(_junction['location']) >= 3:
                        enclosed_junctions.append(_junction)
                        #print "found enclosed junction at ", _start, _end
                        #print _junction['location']
            for helix in self.helices:
                _start = helix['location'][0][0]
                _end = helix['location'][-1][-1]
                if _start >= start and _end <= end:
                    enclosed_helices.append(helix)
            #print "enclosed apical loops", len(enclosed_apical_loops)
            #print "enclosed junctions", len(enclosed_junctions)
            if len(enclosed_apical_loops) == 1 and not enclosed_junctions:
                stem_loop = {'location': [[start, end]]}
                stem_loop['apical_loop'] = enclosed_apical_loops[0]
                stem_loop['inner_loops'] = enclosed_inner_loops
                stem_loop['helices'] = enclosed_helices
                self.stem_loops.append(stem_loop)

        self.stem_loops = sorted(self.stem_loops, key=lambda x: x['apical_loop']['location'][0])

    def find_connected_modules(self):
        self.connected_modules = []
        if not self.junctions:
            self.find_junctions()
        if not self.stem_loops:
            self.find_stem_loops()

        tertiary_interactions = self.tertiary_interactions

        for tertiary_interaction in self.tertiary_interactions:
            start = tertiary_interaction['location'][0][0]
            end = tertiary_interaction['location'][-1][-1]
            #print "Tertiary Interaction",start, end
            for stem_loop_1 in self.stem_loops:
                location_1 = Location(nested_lists = stem_loop_1['location'])
                if location_1.has_position(start):
                    for junction in self.junctions:
                        if len(junction['location']) >=3 :
                            location_2 = Location(nested_lists = junction['location'])
                            if location_2.has_position(end):
                                if location_2.end() < location_1.start() or location_2.start() > location_1.end():
                                    self.connected_modules.append((stem_loop_1, junction))
                                    #print location_1.start(),location_1.end()
                                    #print location_2.start(),location_2.end()
                    for stem_loop_2 in self.stem_loops:
                        location_2 = Location(nested_lists = stem_loop_2['location'])
                        if location_2.has_position(end) and stem_loop_2 != stem_loop_1:
                            if location_2.end() < location_1.start() or location_2.start() > location_1.end():
                                self.connected_modules.append((stem_loop_1, stem_loop_2))
                                #print location_1.start(),location_1.end()
                                #print location_2.start(),location_2.end()
                if location_1.has_position(end):
                    for junction in self.junctions:
                        if len(junction['location']) >=3 :
                            location_2 = Location(nested_lists = junction['location'])
                            if location_2.has_position(start):
                                if location_2.end() < location_1.start() or location_2.start() > location_1.end():
                                    self.connected_modules.append((stem_loop_1, junction))
                                    #print location_1.start(),location_1.end()
                                    #print location_2.start(),location_2.end()

    def add_helix(self, name, start, end, length):
        _ends = [start, start+length-1, end-length+1, end]
        #no pseudoknot allowed
        for helix in self.helices:
            ends = [helix['location'][0][0], helix['location'][0][1], helix['location'][-1][0], helix['location'][-1][-1]]
            if _ends[0] >= ends[1] and _ends[0] <= ends[2] and _ends[3] >= ends[3] or _ends[0] <= ends[0] and _ends[3] >= ends[1] and _ends[3] <= ends[2]: #pseudoknot
                for i in range(0, length):
                    self.add_tertiary_interaction('C', '(', ')', start+i, end-i)
                return None
        helix = {
            'name': name,
            'location': [[start,start+length-1],[end-length+1,end]],
            'length': length,
            'interactions': []
            }
        self.helices.append(helix)
        self.helices = sorted(self.helices, key=lambda helix: helix['location'][0][0]) #the helices are sorted according to the start position
        return helix

    def add_single_strand(self, name, start, length):
        single_strand = {
            'name': name,
            'location': [start,start+length-1]
        };
        self.single_strands.append(single_strand)
        return single_strand

    def add_tertiary_interaction(self, orientation, edge1, edge2, pos1, pos2):
        location = [[pos1, pos1], [pos2, pos2]]
        for tertiary_interaction in self.tertiary_interactions:
            if tertiary_interaction['location'] == location:
                self.tertiary_interactions.remove(tertiary_interaction)
                break
        self.tertiary_interactions.append({
                            'orientation': orientation,
                            'edge1': edge1,
                            'edge2': edge2,
                            'location': [[pos1, pos1], [pos2, pos2]]
                        })

    def add_base_pair(self, orientation, edge1, edge2, pos1, pos2):
        is_secondary_interaction = False
        location = [[pos1, pos1], [pos2, pos2]]
        for helix in self.helices:
            start = helix['location'][0][0]
            end = helix['location'][-1][-1]
            length =  helix['length']

            if pos1 >= start and pos1 <= start+length-1:
                diff = pos1 -start
                if end - pos2 == diff:
                    #if not canonical (not AU, GC or GU, neither cWWW, we add it to the helix as a non-canonical secondary interaction
                    if not (self.rna.sequence[pos1-1] == 'A' and self.rna.sequence[pos2-1] == 'U' or \
                            self.rna.sequence[pos1-1] == 'U' and self.rna.sequence[pos2-1] == 'A' or \
                            self.rna.sequence[pos1-1] == 'G' and self.rna.sequence[pos2-1] == 'C' or \
                            self.rna.sequence[pos1-1] == 'C' and self.rna.sequence[pos2-1] == 'G' or \
                            self.rna.sequence[pos1-1] == 'G' and self.rna.sequence[pos2-1] == 'U' or \
                            self.rna.sequence[pos1-1] == 'U' and self.rna.sequence[pos2-1] == 'G') or \
                          orientation != 'C' or edge1 != '(' or edge2 != ')': #we have a non-canonical secondary-interaction

                        for secondary_interaction in helix['interactions']:
                            if secondary_interaction['location'] == location:
                                helix['interactions'].remove(secondary_interaction)
                                break

                        helix['interactions'].append({
                            'orientation': orientation,
                            'edge1': edge1,
                            'edge2': edge2,
                            'location': location
                        })
                    is_secondary_interaction = True
                    break

        if not is_secondary_interaction:
            #if we reach this point, its a tertiary interaction
            self.add_tertiary_interaction(orientation, edge1, edge2, pos1, pos2)

class TertiaryStructure:

    def __init__(self, rna):
        self.source = 'N.A.:N.A.:N.A.'
        self.rna = rna
        self.name = "N.A."
        self.residues = {} #the keys are the absolute position of residues
        self.numbering_system = {}
        self._id = str(ObjectId())

    def get_atoms(self):
        """
        Returns:
        ------
        the description of atoms in a panda dataframe. Columns are:
        - atom name
        - residue absolute position
        - residue position label (according to the numbering system)
        - residue name
        - chain name
        - x (float)
        - y (float)
        - z (float)
        """
        _atoms = []
        keys =[]
        for k in self.residues:
            keys.append(k)

        keys.sort() #the absolute position are sorted

        for key in keys:
            atoms = self.residues[key]['atoms']
            for atom in atoms:
                _atoms.append({
                    'name': atom['name'],
                    'absolute position': key,
                    'position label': self.get_residue_label(key),
                    'residue name': self.rna.sequence[key-1],
                    'chain name': self.rna.name,
                    'x': atom['coords'][0],
                    'y': atom['coords'][1],
                    'z': atom['coords'][2]
                })

        return DataFrame(_atoms)

    def add_atom(self, atom_name, absolute_position, coords):
        atom_name = re.sub("\*", "'", atom_name)
        if atom_name == 'OP1':
            atom_name = 'O1P'
        elif atom_name == 'OP2':
            atom_name = 'O2P'
        elif atom_name == 'OP3':
            atom_name = 'O3P'
        if absolute_position in self.residues:
            self.residues[absolute_position]['atoms'].append({
                    'name': atom_name,
                    'coords': coords
                })
        else:
             self.residues[absolute_position] = {
                'atoms': [{
                    'name': atom_name,
                    'coords': coords
                }]
             }

    def get_residue_label(self, absolute_position):
        if str(absolute_position) in self.numbering_system:
            return self.numbering_system[str(absolute_position)]
        else:
            return str(absolute_position)

def to_pdb(tertiary_structure, location = None, export_numbering_system = False):
    """
    Convert a TertiaryStructure object into PDB data

    Parameters:
    ---------
    - tertiary_structure: a TertiaryStructure object (see pyrna.features)
    - location (default: None): a Location object (see pyrna.features). Restrict the export to the atoms of the residues enclosed by this location.
    - export_numbering_system (default: False): export the numbering system. If False, the residues are numbered from 1 to the length of the molecular chain

    Returns:
    ------
    the PDB data as a String
    """
    lines= []
    i = 1
    keys = []

    for k in tertiary_structure.residues:
        if location and location.has_position(k) or not location:
            keys.append(k)

    keys.sort() #the absolute position are sorted

    for key in keys:
        atoms = tertiary_structure.residues[key]['atoms']
        for atom in atoms:
            if export_numbering_system:
                lines.append("%-6s%5u  %-4s%3s %s%4s    %8.3f%8.3f%8.3f"%("ATOM", i, atom['name'], tertiary_structure.rna.sequence[key-1], tertiary_structure.rna.name[0], tertiary_structure.get_residue_label(key), atom['coords'][0], atom['coords'][1], atom['coords'][2]))
            else:
                lines.append("%-6s%5u  %-4s%3s %s%4u    %8.3f%8.3f%8.3f"%("ATOM", i, atom['name'], tertiary_structure.rna.sequence[key-1], tertiary_structure.rna.name[0], key, atom['coords'][0], atom['coords'][1], atom['coords'][2]))
            i += 1

    lines.append("END")

    return '\n'.join(lines)

def parse_pdb(pdb_data):
    """
    Parse PDB data.

    Parameters:
    ---------
     - pdb_data: the PDB data as a String

    Returns:
    ------
    a list of TertiaryStructure objects (see pyrna.features). if the PDB data describes a tertiary structure made with several molecular chains, this method will return one TertiaryStructure object per chain.
    """
    molecules = []
    chains = []
    tertiary_structures = []
    current_chain = None
    current_residue = None
    current_residue_pos = None
    absolute_position = -1
    current_molecule = None
    residues = []
    current_3D = None
    title = "N.A."

    for line in pdb_data.split('\n'):
        header = line[0:6].strip()
        atom_name = line[12:16].strip()
        residue_name = line[17:20].strip().upper()
        chain_name = line[21:22].strip()
        residue_pos = line[22:27].strip()

        if (header == "ATOM" or header == "HETATM") and not residue_name in ["FMN","PRF","HOH","MG","OHX","MN","ZN", "SO4", "CA", "UNK", "AMO"] and not atom_name in ["MG","K", "NA", "SR", "CL", "CD", "ACA"] and len(chain_name):
            if chain_name != current_chain: #new chain
                current_residue = residue_name
                current_residue_pos = residue_pos
                current_chain = chain_name
                absolute_position = 1
                residues = []
                current_molecule = None
                residues.append(current_residue)
                current_3D = TertiaryStructure(current_molecule)
                current_3D.title = re.sub(' +', ' ', title)
                current_3D.numbering_system[str(absolute_position)] = current_residue_pos

            elif current_residue_pos != residue_pos: # new residue
                current_residue = residue_name
                current_residue_pos = residue_pos
                if current_molecule:
                    current_molecule.add_residue(current_residue)
                else:
                    residues.append(current_residue)
                absolute_position += 1
                current_3D.numbering_system[str(absolute_position)] = current_residue_pos

            x = float(line[30:38].strip())
            y = float(line[38:46].strip())
            z = float(line[46:54].strip())
            current_3D.add_atom(atom_name, absolute_position, [x,y,z])

            if (atom_name == "O4'" or atom_name == "O4*") and not current_molecule in molecules:
                current_molecule = RNA(sequence="", name = current_chain)
                current_3D.rna = current_molecule
                for residue in residues:
                    current_molecule.add_residue(current_residue)
                molecules.append(current_molecule)
                tertiary_structures.append(current_3D)

            elif (atom_name == "CA") and not current_molecule in molecules:
                current_molecule = Protein(sequence="", name = current_chain)
                current_3D.rna = current_molecule
                for residue in residues:
                    current_molecule.add_residue(current_residue)
                molecules.append(current_molecule)
                tertiary_structures.append(current_3D)

        elif header == 'TITLE':
            title += line[10:]

        elif header == "TER":
            current_chain = None
            current_residue_pos = None
            current_molecule = None
            residues = []

    return tertiary_structures

def base_pairs_to_secondary_structure(rna, base_pairs):
    """
    Parameters:
    ---------
    - rna: an RNA object
    - base_pairs: the base pairs listed in a pandas Dataframe

    Returns:
    ------
    a SecondaryStructure object
    """

    ss = SecondaryStructure(rna)

    if not len(base_pairs):
        ss.add_single_strand("SS1", 1, len(rna))
        return ss

    new_helix = False
    helix_start = -1
    helix_end = -1
    helix_length = -1
    base_pairs = base_pairs.sort_values(by='pos1') #the base pairs are sorted according to the first position
    base_pairs = base_pairs.values
    helix_count = 1

    next_pos1 = -1
    next_edge1 = None
    next_pos2 = -1
    next_edge2 = None
    next_orientation = None

    non_canonical_secondary_interactions = []

    for i in range(0, len(base_pairs)-1):
        bp = base_pairs[i]
        pos1 = bp[3]
        edge1 = bp[1]
        pos2 = bp[4]
        edge2 = bp[2]
        orientation = bp[0]

        next_bp = base_pairs[i+1]
        next_pos1 = next_bp[3]
        next_edge1 = next_bp[1]
        next_pos2 = next_bp[4]
        next_edge2 = next_bp[2]
        next_orientation = next_bp[0]

        if pos1+1 == next_pos1 and pos2-1 == next_pos2:
            if new_helix:
                helix_length += 1
                if not is_canonical(rna[next_pos1-1], rna[next_pos2-1], next_orientation, next_edge1, next_edge2):
                    non_canonical_secondary_interactions.append((next_orientation, next_edge1, next_edge2, next_pos1, next_pos2))
            else:
                new_helix = True
                helix_length = 2
                helix_start = pos1
                helix_end = pos2
                if not is_canonical(rna[pos1-1], rna[pos2-1], orientation, edge1, edge2):
                    non_canonical_secondary_interactions.append((orientation, edge1, edge2, pos1, pos2))
                if not is_canonical(rna[next_pos1-1], rna[next_pos2-1], next_orientation, next_edge1, next_edge2):
                    non_canonical_secondary_interactions.append((next_orientation, next_edge1, next_edge2, next_pos1, next_pos2))

        else:
            if new_helix:
                ss.add_helix("H"+str(helix_count), helix_start, helix_end, helix_length)
                helix_count += 1
            else:
                ss.add_tertiary_interaction(orientation, edge1, edge2, pos1, pos2)
            new_helix = False

    #the last helix
    if new_helix:
        ss.add_helix("H"+str(helix_count), helix_start, helix_end, helix_length)
        helix_count+=1
    else:
        ss.add_tertiary_interaction(next_orientation, next_edge1, next_edge2, next_pos1, next_pos2)

    #now we add the non-canonical interactions to their helices
    for non_canonical_secondary_interaction in non_canonical_secondary_interactions:
        ss.add_base_pair( non_canonical_secondary_interaction[0], non_canonical_secondary_interaction[1], non_canonical_secondary_interaction[2], non_canonical_secondary_interaction[3], non_canonical_secondary_interaction[4] )

    #we construct the single-strands
    ss_count = 1
    ss_start = -1
    ss_length = 0
    for i in range(1, len(rna)+1):
        paired_residue = ss.get_paired_residue(i)
        if paired_residue != -1 and ss_length > 0:
            ss.add_single_strand("SS"+str(ss_count), ss_start, ss_length)
            ss_length = 0
            ss_count += 1
        elif paired_residue == -1:
            if ss_length == 0:
                ss_start = i
            ss_length += 1

    #the last single-strand
    if ss_length > 0:
        ss.add_single_strand("SS"+str(ss_count), ss_start, ss_length)

    return ss

modified_aminoacids = {
    "ALA": "A",
    "ARG": "R",
    "ASN": "N",
    "ASP": "D",
    "ASX": "B",
    "CYS": "C",
    "GLU": "E",
    "GLN": "Q",
    "GLX": "Z",
    "GLY": "G",
    "HIS": "H",
    "ILE": "I",
    "LEU":  "L",
    "LYS": "K",
    "MET": "M",
    "PHE": "F",
    "PRO": "P",
    "SER": "S",
    "THR": "T",
    "TRP": "W",
    "TYR": "Y",
    "VAL": "VAL"
}

modified_ribonucleotides = {
    "T": "U",
    "PSU": "U",
    "I": "A",
    "N": "U",
    "S": "U",
    "+A": "A",
    "+C": "C",
    "+G": "G",
    "+I": "I",
    "+T": "U",
    "+U": "U",
    "PU": "A",
    "YG": "G",
    "1AP": "G",
    "1MA": "A",
    "1MG": "G",
    "2DA": "A",
    "2DT": "U",
    "2MA": "A",
    "2MG": "G",
    "4SC": "C",
    "4SU": "U",
    "5IU": "U",
    "5MC": "C",
    "5MU": "U",
    "5NC": "C",
    "6MP": "A",
    "7MG": "G",
    "A23": "A",
    "AD2": "A",
    "AET": "A",
    "AMD": "A",
    "AMP": "A",
    "APN": "A",
    "ATP": "A",
    "AZT": "U",
    "CCC": "C",
    "CMP": "A",
    "CPN": "C",
    "DAD": "A",
    "DCT": "C",
    "DDG": "G",
    "DG3": "G",
    "DHU": "U",
    "DOC": "C",
    "EDA": "A",
    "G7M": "G",
    "GDP": "G",
    "GNP": "G",
    "GPN": "G",
    "GTP": "G",
    "GUN": "G",
    "H2U": "U",
    "HPA": "A",
    "IPN": "U",
    "M2G": "G",
    "MGT": "G",
    "MIA": "A",
    "OMC": "C",
    "OMG": "G",
    "OMU": "U",
    "ONE": "U",
    "P2U": "P",
    "PGP": "G",
    "PPU": "A",
    "PRN": "A",
    "PST": "U",
    "QSI": "A",
    "QUO": "G",
    "RIA": "A",
    "SAH": "A",
    "SAM": "A",
    "T23": "U",
    "T6A": "A",
    "TAF": "U",
    "TLC": "U",
    "TPN": "U",
    "TSP": "U",
    "TTP": "U",
    "UCP": "U",
    "VAA": "A",
    "YYG": "G",
    "70U": "U",
    "12A": "A",
    "2MU": "U",
    "127": "U",
    "125": "U",
    "126": "U",
    "MEP": "U",
    "TLN": "U",
    "ADP": "A",
    "TTE": "U",
    "PYO": "U",
    "SUR": "U",
    "PSD": "A",
    "S4U": "U",
    "CP1": "C",
    "TP1": "U",
    "NEA": "A",
    "GCK": "C",
    "CH": "C",
    "EDC": "G",
    "DFC": "C",
    "DFG": "G",
    "DRT": "U",
    "2AR": "A",
    "8OG": "G",
    "IG": "G",
    "IC": "C",
    "IGU": "G",
    "IMC": "C",
    "GAO": "G",
    "UAR": "U",
    "CAR": "C",
    "PPZ": "A",
    "M1G": "G",
    "ABR": "A",
    "ABS": "A",
    "S6G": "G",
    "HEU": "U",
    "P": "G",
    "DNR": "C",
    "MCY": "C",
    "TCP": "U",
    "LGP": "G",
    "GSR": "G",
    "X": "G",
    "R": "A",
    "Y": "A",
    "E": "A",
    "GSS": "G",
    "THX": "U",
    "6CT": "U",
    "TEP": "G",
    "GN7": "G",
    "FAG": "G",
    "PDU": "U",
    "MA6": "A",
    "UMP": "U",
    "SC": "C",
    "GS": "G",
    "TS": "U",
    "AS": "A",
    "ATD": "U",
    "T3P": "U",
    "5AT": "U",
    "MMT": "U",
    "SRA": "A",
    "6HG": "G",
    "6HC": "C",
    "6HT": "U",
    "6HA": "A",
    "55C": "C",
    "U8U": "U",
    "BRO": "U",
    "BRU": "U",
    "5IT": "U",
    "ADI": "A",
    "5CM": "C",
    "IMP": "G",
    "THM": "U",
    "URI": "U",
    "AMO": "A",
    "FHU": "P",
    "TSB": "A",
    "CMR": "C",
    "RMP": "A",
    "SMP": "A",
    "5HT": "U",
    "RT": "U",
    "MAD": "A",
    "OXG": "G",
    "UDP": "U",
    "6MA": "A",
    "5IC": "C",
    "SPT": "U",
    "TGP": "G",
    "BLS": "A",
    "64T": "U",
    "CB2": "C",
    "DCP": "C",
    "ANG": "G",
    "BRG": "G",
    "Z": "A",
    "AVC": "A",
    "5CG": "G",
    "UDP": "U",
    "UMS": "U",
    "BGM": "G",
    "SMT": "U",
    "DU": "U",
    "CH1": "C",
    "GH3": "G",
    "GNG": "G",
    "TFT": "U",
    "U3H": "U",
    "MRG": "G",
    "ATM": "U",
    "GOM": "A",
    "UBB": "U",
    "A66": "A",
    "T66": "U",
    "C66": "C",
    "3ME": "A",
    "A3P": "A",
    "ANP": "A",
    "FA2": "A",
    "9DG": "G",
    "GMU": "U",
    "UTP": "U",
    "5BU": "U",
    "APC": "A",
    "DI": "I",
    "UR3": "U",
    "3DA": "A",
    "DDY": "C",
    "TTD": "U",
    "TFO": "U",
    "TNV": "U",
    "MTU": "U",
    "6OG": "G",
    "E1X": "A",
    "FOX": "A",
    "CTP": "C",
    "D3T": "U",
    "TPC": "C",
    "7DA": "A",
    "7GU": "U",
    "2PR": "A",
    "CBR": "C",
    "I5C": "C",
    "5FC": "C",
    "GMS": "G",
    "2BT": "U",
    "8FG": "G",
    "MNU": "U",
    "AGS": "A",
    "NMT": "U",
    "NMS": "U",
    "UPG": "U",
    "G2P": "G",
    "2NT": "U",
    "EIT": "U",
    "TFE": "U",
    "P2T": "U",
    "2AT": "U",
    "2GT": "U",
    "2OT": "U",
    "BOE": "U",
    "SFG": "G",
    "CSL": "I",
    "PPW": "G",
    "IU": "U",
    "D5M": "A",
    "ZDU": "U",
    "DGT": "U",
    "UD5": "U",
    "S4C": "C",
    "DTP": "A",
    "5AA": "A",
    "2OP": "A",
    "PO2": "A",
    "DC": "C",
    "DA": "A",
    "LOF": "A",
    "ACA": "A",
    "BTN": "A",
    "PAE": "A",
    "SPS": "A",
    "TSE": "A",
    "A2M": "A",
    "NCO": "A",
    "A5M": "C",
    "M5M": "C",
    "S2M": "U",
    "MSP": "A",
    "P1P": "A",
    "N6G": "G",
    "MA7": "A",
    "FE2": "G",
    "AKG": "G",
    "SIN": "G",
    "PR5": "G",
    "GOL": "G",
    "XCY": "G",
    "5HU": "U",
    "CME": "C",
    "EGL": "G",
    "LC": "C",
    "LHU": "U",
    "LG": "G",
    "PUY": "U",
    "PO4": "U",
    "PQ1": "U",
    "ROB": "U",
    "O2C": "C",
    "C30": "C",
    "C31": "C",
    "C32": "C",
    "C33": "C",
    "C34": "C",
    "C35": "C",
    "C36": "C",
    "C37": "C",
    "C38": "C",
    "C39": "C",
    "C40": "C",
    "C41": "C",
    "C42": "C",
    "C43": "C",
    "C44": "C",
    "C45": "C",
    "C46": "C",
    "C47": "C",
    "C48": "C",
    "C49": "C",
    "C50": "C",
    "A30": "A",
    "A31": "A",
    "A32": "A",
    "A33": "A",
    "A34": "A",
    "A35": "A",
    "A36": "A",
    "A37": "A",
    "A38": "A",
    "A39": "A",
    "A40": "A",
    "A41": "A",
    "A42": "A",
    "A43": "A",
    "A44": "A",
    "A45": "A",
    "A46": "A",
    "A47": "A",
    "A48": "A",
    "A49": "A",
    "A50": "A",
    "G30": "G",
    "G31": "G",
    "G32": "G",
    "G33": "G",
    "G34": "G",
    "G35": "G",
    "G36": "G",
    "G37": "G",
    "G38": "G",
    "G39": "G",
    "G40": "G",
    "G41": "G",
    "G42": "G",
    "G43": "G",
    "G44": "G",
    "G45": "G",
    "G46": "G",
    "G47": "G",
    "G48": "G",
    "G49": "G",
    "G50": "G",
    "T30": "U",
    "T31": "U",
    "T32": "U",
    "T33": "U",
    "T34": "U",
    "T35": "U",
    "T36": "U",
    "T37": "U",
    "T38": "U",
    "T39": "U",
    "T40": "U",
    "T41": "U",
    "T42": "U",
    "T43": "U",
    "T44": "U",
    "T45": "U",
    "T46": "U",
    "T47": "U",
    "T48": "U",
    "T49": "U",
    "T50": "U",
    "U30": "U",
    "U31": "U",
    "U32": "U",
    "U33": "U",
    "U34": "U",
    "U35": "U",
    "U36": "U",
    "U37": "U",
    "U38": "U",
    "U39": "U",
    "U40": "U",
    "U41": "U",
    "U42": "U",
    "U43": "U",
    "U44": "U",
    "U45": "U",
    "U46": "U",
    "U47": "U",
    "U48": "U",
    "U49": "U",
    "U50": "U",
    "UFP": "U",
    "UFR": "U",
    "UCL": "U",
    "3DR": "U",
    "CBV": "C",
    "HFA": "A",
    "MMA": "A",
    "DCZ": "C",
    "GNE": "C",
    "A1P": "A",
    "6IA": "A",
    "CTG": "G",
    "5FU": "U",
    "2AD": "A",
    "T2T": "U",
    "XUG": "G",
    "2ST": "U",
    "5PY": "U",
    "4PC": "C",
    "US1": "U",
    "M5C": "C",
    "DG": "G",
    "DA": "A",
    "DT": "U",
    "DC": "C",
    "P5P": "A",
    "FMU": "U"
}

def import_3Ds(db_host = 'localhost', db_port = 27017, rna3dhub = False, canonical_only = True, annotate = False, limit = 5000):
    client = MongoClient(db_host, db_port)
    db_name = ""

    if rna3dhub:
        db_name = "RNA3DHub"
    else:
        rna3dHub = None
        db_name = "PDB"

    db = client[db_name]
    rnaview = Rnaview()

    if not rna3dhub:
        pdb = PDB()
        query ="""<orgPdbQuery>
    <version>head</version>
    <queryType>org.pdb.query.simple.ChainTypeQuery</queryType>
    <description>Chain Type: there is a Protein and a RNA chain but not any DNA or Hybrid</description>
    <containsProtein>N</containsProtein>
    <containsDna>N</containsDna>
    <containsRna>Y</containsRna>
    <containsHybrid>N</containsHybrid>
  </orgPdbQuery>"""
        pdb_ids = pdb.query(query)
        print("%i 3Ds to process"%len(pdb_ids))

        for pdb_id in pdb_ids:
            if db['tertiaryStructures'].find_one({'source':"db:pdb:%s"%pdb_id}):
                continue
            print("Recover %s"%pdb_id)
            for ts in parse_pdb(pdb.get_entry(pdb_id)):
                try:
                    ss = None
                    if annotate:
                        ss, ts = rnaview.annotate(ts, canonical_only = canonical_only)
                    save(db, ss, ts, pdb_id, limit)

                except Exception as e:
                    print(e)
                    print("No annotation for %s"%pdb_id)
                    save(db, None, ts, pdb_id, limit)
    else:
        pdb = PDB()
        rna3dHub = RNA3DHub()
        clusters = rna3dHub.get_clusters()
        print("%i 3Ds to process"%len(clusters))

        for cluster in clusters['pdb-ids']:
            pdb_id = cluster[0].split('|')[0]
            if db['tertiaryStructures'].find_one({'source':"db:pdb:%s"%pdb_id}):
                continue
            print("Recover %s"%pdb_id) #we use the first pdb_id in the list of ids making a cluster
            for ts in parse_pdb(pdb.get_entry(pdb_id)):
                try:
                    ss = None
                    if annotate:
                        ss, ts = rnaview.annotate(ts, canonical_only = canonical_only)
                    save(db, ss, ts, pdb_id, limit)

                except Exception as e:
                    print(e)
                    print("No annotation for %s"%pdb_id)
                    save(db, None, ts, pdb_id, limit)

def save(db, secondary_structure, tertiary_structure, pdbId, limit):
    if db['junctions'].count_documents({}) >= limit:
        print("Limit of %i junctions reached"%limit)
        sys.exit()

    tertiary_structure.source="db:pdb:%s"%pdbId

    if secondary_structure:

        computation = {
            'inputs': [tertiary_structure._id+"@tertiaryStructures"],
            'outputs': [secondary_structure._id+"@secondaryStructures"],
            'tool': "tool:rnaview:N.A.",
            'date': str(datetime.datetime.now())
        }

        if secondary_structure.rna == tertiary_structure.rna:
            ncRNA = {
                '_id': secondary_structure.rna._id,
                'source': secondary_structure.rna.source,
                'name': secondary_structure.rna.name,
                'sequence': secondary_structure.rna.sequence,
            }
            if not db['ncRNAs'].find_one({'_id':ncRNA['_id']}):
                db['ncRNAs'].insert_one(ncRNA)
        else:
            ncRNA = {
                '_id': secondary_structure.rna._id,
                'source': secondary_structure.rna.source,
                'name': secondary_structure.rna.name,
                'sequence': secondary_structure.rna.sequence,
            }
            if not db['ncRNAs'].find_one({'_id':ncRNA['_id']}):
                db['ncRNAs'].insert_one(ncRNA)
            ncRNA = {
                '_id': tertiary_structure.rna._id,
                'source': tertiary_structure.rna.source,
                'name': tertiary_structure.rna.name,
                'sequence': tertiary_structure.rna.sequence,
            }
            if not db['ncRNAs'].find_one({'_id':ncRNA['_id']}):
                db['ncRNAs'].insert_one(ncRNA)

        secondary_structure.find_junctions()

        ss_descr = {
            '_id': secondary_structure._id,
            'source': secondary_structure.source,
            'name': secondary_structure.name,
            'rna': secondary_structure.rna._id+"@ncRNAs"
        }

        helices_descr = []
        for helix in secondary_structure.helices:
            helix_desc = {
                'name': helix['name'],
                'location': helix['location']
            }
            if 'interactions' in helix:
                interactions_descr = []
                for interaction in helix['interactions']:
                    interactions_descr.append({
                        'orientation': interaction['orientation'],
                        'edge1': interaction['edge1'],
                        'edge2': interaction['edge2'],
                        'location': interaction['location']
                    })
                helix_desc['interactions'] = interactions_descr

            helices_descr.append(helix_desc)

        ss_descr['helices'] = helices_descr

        single_strands_descr = []
        for single_strand in secondary_structure.single_strands:
            single_strands_descr.append({
                'name': single_strand['name'],
                'location': single_strand['location']
            })

        ss_descr['singleStrands'] = single_strands_descr

        tertiary_interactions_descr = []
        for tertiary_interaction in secondary_structure.tertiary_interactions:
            tertiary_interactions_descr.append({
                'orientation': tertiary_interaction['orientation'],
                'edge1': tertiary_interaction['edge1'],
                'edge2': tertiary_interaction['edge2'],
                'location': tertiary_interaction['location']
            })

        ss_descr['tertiaryInteractions'] = tertiary_interactions_descr

        db['secondaryStructures'].insert_one(ss_descr)

    ncRNA = {
        '_id': tertiary_structure.rna._id,
        'source': tertiary_structure.rna.source,
        'name': tertiary_structure.rna.name,
        'sequence': tertiary_structure.rna.sequence,
    }
    if not db['ncRNAs'].find_one({'_id':ncRNA['_id']}):
        db['ncRNAs'].insert_one(ncRNA)

    ts_descr = {
        '_id': tertiary_structure._id,
        'source': tertiary_structure.source,
        'name': tertiary_structure.name,
        'rna': tertiary_structure.rna._id+"@ncRNAs",
        'numbering-system': tertiary_structure.numbering_system
    }

    residues_descr = {}
    keys=[]
    for k in tertiary_structure.residues:
        keys.append(k)

    keys.sort() #the absolute position are sorted

    for key in keys:
        atoms = tertiary_structure.residues[key]['atoms']

        atoms_descr = []

        for atom in atoms:
            atoms_descr.append({
                'name': atom['name'],
                'coords': atom['coords']
            })
        residues_descr[str(key)] = {
            'atoms': atoms_descr
        }

    ts_descr['residues'] = residues_descr

    if not db['tertiaryStructures'].find_one({'_id':ts_descr['_id']}):
        db['tertiaryStructures'].insert_one(ts_descr)

        if secondary_structure:

            for junction in secondary_structure.junctions:
                junction_descr = {
                    '_id': str(ObjectId()),
                    'molecule': secondary_structure.rna._id+"@ncRNAs",
                    'tertiary-structure': {
                        'id':tertiary_structure._id+'@tertiaryStructures',
                        'source': tertiary_structure.source
                    },
                    'description': junction['description'],
                    'location': junction['location']
                }
                computation['outputs'].append(junction_descr['_id']+"@junctions")

                db['junctions'].insert_one(junction_descr)

            db['computations'].insert_one(computation)

if __name__ == '__main__':
    db_host = 'localhost'
    db_port = 27017
    rna3dhub = False
    canonical_only = False
    annotate = False
    limit = 5000

    if "-h" in sys.argv:
        print("Usage: ./import_3Ds.py [-p x] [-mh x] [-mp x] [-l x] [-rna3dhub] [-canonical_only] [-annotate]")
        print('- mh: the mongodb host (default: localhost)\n')
        print('- mp: the mongodb port (default: 27017)\n')
        print('- l: limit of junctions to be stored (default: 5000)\n')
        print('- rna3dhub: use the 3D structures from the non-redundant set\n')
        print('- canonical_only: a secondary structure is made with canonical base-pairs only')
        print('- annotate: annotate each 3D structure imported')
        sys.exit(-1)

    if "-mh" in sys.argv:
        db_host = sys.argv[sys.argv.index("-mh")+1]
    if "-mp" in sys.argv:
        db_port = int(sys.argv[sys.argv.index("-mp")+1])
    if "-l" in sys.argv:
        limit = int(sys.argv[sys.argv.index("-l")+1])
    rna3dhub =  "-rna3dhub" in sys.argv
    canonical_only =  "-canonical_only" in sys.argv
    annotate = "-annotate" in sys.argv

    import_3Ds(db_host = db_host, db_port = db_port, rna3dhub = rna3dhub, canonical_only = canonical_only, annotate = annotate, limit = limit)
